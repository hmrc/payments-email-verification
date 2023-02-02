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

package uk.gov.hmrc.paymentsemailverification.repositories

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs
import uk.gov.hmrc.paymentsemailverification.config.AppConfig
import uk.gov.hmrc.paymentsemailverification.crypto.CryptoFormat.OperationalCryptoFormat
import uk.gov.hmrc.paymentsemailverification.models.{CorrelationId, EmailVerificationStatus, GGCredId}
import uk.gov.hmrc.paymentsemailverification.repositories.Repo.{Id, IdExtractor}
import uk.gov.hmrc.paymentsemailverification.repositories.EmailVerificationStatusRepo._

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
final class EmailVerificationStatusRepo @Inject() (
    mongoComponent: MongoComponent,
    config:         AppConfig
)(implicit ec: ExecutionContext, cryptoFormat: OperationalCryptoFormat)
  extends Repo[CorrelationId, EmailVerificationStatus](
    collectionName = "emailVerificationStatus",
    mongoComponent = mongoComponent,
    indexes        = EmailVerificationStatusRepo.indexes(config.emailVerificationStatusRepoTtl.toSeconds),
    extraCodecs    = Codecs.playFormatCodecsBuilder(EmailVerificationStatus.format).build,
    replaceIndexes = true
  ) {

  def findAllEntries(ggCredId: GGCredId): Future[List[EmailVerificationStatus]] =
    collection
      .find(filter = Filters.eq("credId", ggCredId.value))
      .sort(BsonDocument("createdAt" -> -1))
      .toFuture().map(_.toList)

  def update(emailVerificationStatus: EmailVerificationStatus): Future[Unit] = upsert(emailVerificationStatus)
}

object EmailVerificationStatusRepo {

  /**
   * I've used correlationId as it saves us from having to create a new value to be used for _id.
   * We can always change this, but it kind of makes sense since there will be a value linkable between this db and journey db.
   */
  implicit val correlationId: Id[CorrelationId] = (i: CorrelationId) => i.value.toString
  implicit val correlationIdExtractor: IdExtractor[EmailVerificationStatus, CorrelationId] =
    (emailVerificationStatus: EmailVerificationStatus) => CorrelationId(emailVerificationStatus._id)

  def indexes(cacheTtlInSeconds: Long): Seq[IndexModel] = Seq(
    IndexModel(
      keys         = Indexes.ascending("lastUpdated"),
      indexOptions = IndexOptions().expireAfter(cacheTtlInSeconds, TimeUnit.SECONDS).name("lastUpdatedIdx")
    ),
    IndexModel(
      keys         = Indexes.ascending("credId"),
      indexOptions = IndexOptions().name("credIdIdx")
    )
  )
}

