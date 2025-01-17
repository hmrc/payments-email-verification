/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.paymentsemailverification.testsupport

import org.apache.pekko.util.Timeout
import com.google.inject.{AbstractModule, Provides}
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.Helpers.await
import org.mongodb.scala.SingleObservableFuture
import play.api.test.{DefaultTestServerFactory, RunningServer}
import play.api.{Application, Mode}
import play.core.server.ServerConfig
import uk.gov.hmrc.crypto.{AesCrypto, Decrypter, Encrypter}
import uk.gov.hmrc.paymentsemailverification.crypto.CryptoFormat.OperationalCryptoFormat
import uk.gov.hmrc.paymentsemailverification.models.CorrelationId
import uk.gov.hmrc.paymentsemailverification.repositories.EmailVerificationStatusRepo
import uk.gov.hmrc.paymentsemailverification.services.CorrelationIdGenerator

import java.time.{Clock, ZoneId}
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Singleton
import scala.concurrent.duration._

trait ItSpec
  extends AnyFreeSpecLike,
    RichMatchers,
    GuiceOneServerPerSuite,
    WireMockSupport { self =>

  override def beforeEach(): Unit = {
    super.beforeEach()

    await(app.injector.instanceOf[EmailVerificationStatusRepo].collection.drop().toFuture())(Timeout(10.seconds))
    ()
  }

  implicit val testCrypto: Encrypter with Decrypter = new AesCrypto {
    override protected val encryptionKey: String = "P5xsJ9Nt+quxGZzB4DeLfw=="
  }

  val frozenClock: Clock = Clock.fixed(TestData.frozenInstant, ZoneId.of("Z"))

  lazy val overridingsModule: AbstractModule = new AbstractModule {
    override def configure(): Unit = ()

    @Provides
    @Singleton
    def clock: Clock = frozenClock

    @Provides
    @Singleton
    def operationalCryptoFormat: OperationalCryptoFormat = OperationalCryptoFormat(testCrypto)

    @Provides
    @Singleton
    def testCorrelationIdGenerator(testCorrelationIdGenerator: TestCorrelationIdGenerator): CorrelationIdGenerator =
      testCorrelationIdGenerator

    @Provides
    @Singleton
    def testCorrelationIdGenerator(): TestCorrelationIdGenerator = {
      val randomPart: String = UUID.randomUUID().toString.take(8)
      val correlationIdPrefix: TestCorrelationIdPrefix = TestCorrelationIdPrefix(s"$randomPart-843f-4988-89c6-d4d3e2e91e26")
      new TestCorrelationIdGenerator(correlationIdPrefix)
    }
  }

  def correlationIdGenerator: TestCorrelationIdGenerator = app.injector.instanceOf[TestCorrelationIdGenerator]

  val testServerPort: Int = 19001

  val baseUrl: String = s"http://localhost:${testServerPort.toString}"
  val databaseName: String = "payments-email-verification-it"

  def conf: Map[String, Any] = Map(
    "mongodb.uri" -> s"mongodb://localhost:27017/$databaseName",
    "microservice.services.payments-email-verification.protocol" -> "http",
    "microservice.services.payments-email-verification.host" -> "localhost",
    "microservice.services.payments-email-verification.port" -> testServerPort,
    "microservice.services.auth.port" -> WireMockSupport.port,
    "microservice.services.email-verification.port" -> WireMockSupport.port,
    "auditing.enabled" -> false,
    "auditing.traceRequests" -> false
  )

  //in tests use `app`
  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(conf)
    .overrides(GuiceableModule.fromGuiceModules(Seq(overridingsModule)))
    .build()

  object TestServerFactory extends DefaultTestServerFactory {
    override protected def serverConfig(app: Application): ServerConfig = {
      val sc = ServerConfig(port    = Some(testServerPort), sslPort = Some(0), mode = Mode.Test, rootDir = app.path)
      sc.copy(configuration = sc.configuration.withFallback(overrideServerConfiguration(app)))
    }
  }

  given RunningServer =
    TestServerFactory.start(app)

}

final case class TestCorrelationIdPrefix(value: String)

class TestCorrelationIdGenerator(testCorrelationIdPrefix: TestCorrelationIdPrefix) extends CorrelationIdGenerator {
  private val correlationIdIterator: Iterator[CorrelationId] =
    LazyList.from(0).map(i => CorrelationId(UUID.fromString(s"${testCorrelationIdPrefix.value.dropRight(1)}${i.toString}"))).iterator
  private val nextCorrelationIdCached = new AtomicReference[CorrelationId](correlationIdIterator.next())

  def readNextCorrelationId(): CorrelationId = nextCorrelationIdCached.get()

  override def nextCorrelationId(): CorrelationId = {
    nextCorrelationIdCached.getAndSet(correlationIdIterator.next())
  }
}
