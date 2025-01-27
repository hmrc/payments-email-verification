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

package uk.gov.hmrc.paymentsemailverification.controllers

import paymentsEmailVerification.connectors.PaymentsEmailVerificationConnector
import paymentsEmailVerification.models.{Email, EmailVerificationResult, EmailVerificationState, NumberOfPasscodeJourneysStarted}
import paymentsEmailVerification.models.api.{GetEmailVerificationResultRequest, StartEmailVerificationJourneyRequest, StartEmailVerificationJourneyResponse}
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.paymentsemailverification.models.emailverification.EmailVerificationResultResponse.EmailResult
import uk.gov.hmrc.paymentsemailverification.models.emailverification.RequestEmailVerificationSuccess
import uk.gov.hmrc.paymentsemailverification.models.{EmailVerificationStatus, EncryptedEmail, GGCredId}
import uk.gov.hmrc.paymentsemailverification.repositories.EmailVerificationStatusRepo
import uk.gov.hmrc.paymentsemailverification.testsupport.stubs.{AuthStub, EmailVerificationStub}
import uk.gov.hmrc.paymentsemailverification.testsupport.{ItSpec, TestData}

import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, ZoneOffset}
import java.util.{Locale, UUID}
import scala.concurrent.Future

class EmailVerificationControllerSpec extends ItSpec {

  val connector = app.injector.instanceOf[PaymentsEmailVerificationConnector]

  val emailVerificationStatusRepo: EmailVerificationStatusRepo = app.injector.instanceOf[EmailVerificationStatusRepo]

  given HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(TestData.authToken)))

  "POST /email-verification/start" - {

    val startEmailVerificationJourneyRequest = StartEmailVerificationJourneyRequest(
      "continue",
      "origin",
      "deskpro",
      "accessibility",
      "title",
      "back",
      "enter",
      Email(s"email${UUID.randomUUID().toString}@test.com"),
      "en"
    )

      def emailVerificationStatus(ggCredId: GGCredId, email: Email): EmailVerificationStatus = EmailVerificationStatus(
        _id                             = UUID.randomUUID(),
        credId                          = ggCredId,
        email                           = EncryptedEmail(SensitiveString(email.value)),
        numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(1),
        verificationResult              = None,
        createdAt                       = TestData.frozenInstant,
        lastUpdated                     = TestData.frozenInstant
      )

    behave like authenticatedEndpointBehaviour(connector.startEmailVerification(startEmailVerificationJourneyRequest)(using _))

    "return a redirect url if a journey is successfully started" in {
      val redirectUri: String = "/redirect"
      val id = correlationIdGenerator.readNextCorrelationId()

      AuthStub.authorise()
      EmailVerificationStub.requestEmailVerification(Right(RequestEmailVerificationSuccess(redirectUri)))

      val result = connector.startEmailVerification(startEmailVerificationJourneyRequest)
      await(result) shouldBe StartEmailVerificationJourneyResponse.Success(
        s"http://localhost:9890$redirectUri"
      )

      await(emailVerificationStatusRepo.findAllEntries(TestData.ggCredId)) shouldBe List(
        emailVerificationStatus(TestData.ggCredId, startEmailVerificationJourneyRequest.email).copy(_id = id.value)
      )

      EmailVerificationStub.verifyRequestEmailVerification(startEmailVerificationJourneyRequest, TestData.ggCredId)
    }

    "maintain the redirectUri in the email verification response if the environment is local and the uri is absolute" in {
      val redirectUri: String = "http://localhost:12345/redirect"

      AuthStub.authorise()
      EmailVerificationStub.requestEmailVerification(Right(RequestEmailVerificationSuccess(redirectUri)))

      val result = connector.startEmailVerification(startEmailVerificationJourneyRequest)
      await(result) shouldBe StartEmailVerificationJourneyResponse.Success(redirectUri)

      EmailVerificationStub.verifyRequestEmailVerification(startEmailVerificationJourneyRequest, TestData.ggCredId)
    }

    "return a locked response if the email-verification service gives a 401 (UNAUTHORIZED) response" in {
      AuthStub.authorise()
      EmailVerificationStub.requestEmailVerification(Left(UNAUTHORIZED))

      val result = connector.startEmailVerification(startEmailVerificationJourneyRequest)
      await(result) shouldBe StartEmailVerificationJourneyResponse.Error(EmailVerificationState.TooManyPasscodeAttempts)

      EmailVerificationStub.verifyRequestEmailVerification(startEmailVerificationJourneyRequest, TestData.ggCredId)
    }

    "return 'AlreadyVerified' and not call email-verification if email is already verified" in {
      AuthStub.authorise()

      val oldEmailVerificationStatus =
        emailVerificationStatus(TestData.ggCredId, startEmailVerificationJourneyRequest.email)
          .copy(verificationResult = Some(EmailVerificationResult.Verified))

      emailVerificationStatusRepo.upsert(oldEmailVerificationStatus).futureValue

      val result = connector.startEmailVerification(startEmailVerificationJourneyRequest)
      await(result) shouldBe StartEmailVerificationJourneyResponse.Error(EmailVerificationState.AlreadyVerified)

      await(emailVerificationStatusRepo.findAllEntries(TestData.ggCredId)) shouldBe List(
        oldEmailVerificationStatus.copy(numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(2))
      )

      EmailVerificationStub.verifyNoneRequestVerification()
    }

    "return 'TooManyPasscodeAttempts' if that is the status in mongo" in {
      AuthStub.authorise()

      emailVerificationStatusRepo.upsert(emailVerificationStatus(TestData.ggCredId, startEmailVerificationJourneyRequest.email)
        .copy(verificationResult = Some(EmailVerificationResult.Locked))).futureValue

      val result = connector.startEmailVerification(startEmailVerificationJourneyRequest)
      await(result) shouldBe StartEmailVerificationJourneyResponse.Error(EmailVerificationState.TooManyPasscodeAttempts)
      EmailVerificationStub.verifyNoneRequestVerification()
    }

    "return 'TooManyPasscodeJourneysStarted' if that is the status in mongo" in {
      AuthStub.authorise()

      emailVerificationStatusRepo.upsert(emailVerificationStatus(TestData.ggCredId, startEmailVerificationJourneyRequest.email)
        .copy(numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(5))).futureValue

      val result = connector.startEmailVerification(startEmailVerificationJourneyRequest)
      await(result) shouldBe StartEmailVerificationJourneyResponse.Error(EmailVerificationState.TooManyPasscodeJourneysStarted)
      EmailVerificationStub.verifyNoneRequestVerification()
    }

    "return 'TooManyDifferentEmailAddresses' if that is the status in mongo" in {
      AuthStub.authorise()

      val emailVerificationStatus: EmailVerificationStatus = EmailVerificationStatus(
        _id                             = UUID.randomUUID(),
        credId                          = TestData.ggCredId,
        email                           = EncryptedEmail(SensitiveString(startEmailVerificationJourneyRequest.email.value)),
        numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(1),
        verificationResult              = None,
        createdAt                       = TestData.frozenInstant,
        lastUpdated                     = TestData.frozenInstant
      )
      val nineOtherEntries: List[EmailVerificationStatus] =
        (1 to 9).toList.map(_ => emailVerificationStatus.copy(
          _id   = UUID.randomUUID(),
          email = EncryptedEmail(SensitiveString(s"email${UUID.randomUUID().toString}@test.com"))
        ))

      (emailVerificationStatus :: nineOtherEntries).foreach(emailVerificationStatusRepo.upsert(_).futureValue)
      emailVerificationStatusRepo.findAllEntries(TestData.ggCredId).futureValue.size shouldBe 10

      val result = connector.startEmailVerification(startEmailVerificationJourneyRequest)
      await(result) shouldBe StartEmailVerificationJourneyResponse.Error(EmailVerificationState.TooManyDifferentEmailAddresses)
      EmailVerificationStub.verifyNoneRequestVerification()
    }

  }

  "POST /email-verification/status" - {

    behave like authenticatedEndpointBehaviour(
      connector.getEmailVerificationResult(GetEmailVerificationResultRequest(Email("email")))(using _)
    )

    "return a 'Verified' response if the email address has been verified with the GG cred id" in {
      val email = Email(s"email${UUID.randomUUID().toString}@test.com")
      val getResultRequest = GetEmailVerificationResultRequest(email)
      val id = correlationIdGenerator.readNextCorrelationId()

      AuthStub.authorise()
      EmailVerificationStub.getVerificationResult(
        TestData.ggCredId,
        Right(List(EmailResult(email.value, verified = true, locked = false)))
      )

      val result = connector.getEmailVerificationResult(getResultRequest)
      await(result) shouldBe EmailVerificationResult.Verified

      val emailVerificationStatus = await(emailVerificationStatusRepo.findAllEntries(TestData.ggCredId))
      emailVerificationStatus.size shouldBe 1
      emailVerificationStatus.headOption.map(_._id) shouldBe Some(id.value)
      emailVerificationStatus.headOption.map(_.email.value.decryptedValue) shouldBe Some(email.value)
      emailVerificationStatus.headOption.map(_.numberOfPasscodeJourneysStarted.value) shouldBe Some(1)
      emailVerificationStatus.headOption.flatMap(_.verificationResult) shouldBe Some(EmailVerificationResult.Verified)

      EmailVerificationStub.verifyNoneGetVerificationStatus(TestData.ggCredId)
    }

    "return a 'TooManyPasscodeAttempts' response if the email address has been locked with the GG cred id" in {
      val email = Email(s"email${UUID.randomUUID().toString}@test.com")
      val getResultRequest = GetEmailVerificationResultRequest(email)
      // test an update to an existing status in mongo works
      val oldEmailVerificationStatus = EmailVerificationStatus(
        _id                             = UUID.randomUUID(),
        credId                          = TestData.ggCredId,
        email                           = EncryptedEmail(SensitiveString(email.value)),
        numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(1),
        verificationResult              = Some(EmailVerificationResult.Verified),
        createdAt                       = TestData.frozenInstant,
        lastUpdated                     = TestData.frozenInstant.minusSeconds(100L),
      )

      AuthStub.authorise()
      EmailVerificationStub.getVerificationResult(
        TestData.ggCredId,
        Right(List(EmailResult(email.value, verified = false, locked = true)))
      )

      await(emailVerificationStatusRepo.upsert(oldEmailVerificationStatus))

      val result = connector.getEmailVerificationResult(getResultRequest)
      await(result) shouldBe EmailVerificationResult.Locked

      await(emailVerificationStatusRepo.findAllEntries(TestData.ggCredId)) shouldBe List(
        oldEmailVerificationStatus.copy(
          verificationResult = Some(EmailVerificationResult.Locked),
          lastUpdated        = TestData.frozenInstant
        )
      )

      EmailVerificationStub.verifyNoneGetVerificationStatus(TestData.ggCredId)
    }

    "should throw an error when" - {

      val email = Email(s"email${UUID.randomUUID().toString}@test.com")

      val getResultRequest = GetEmailVerificationResultRequest(email)

        def testIsUpstreamErrorResponse(result: Future[EmailVerificationResult], expectedStatusCode: Int): Unit = {
          result.failed.futureValue match {
            case e: UpstreamErrorResponse => e.statusCode shouldBe expectedStatusCode
            case e                        => fail(s"Expected UpstreamErrorResponse but got ${e.toString}")
          }
          ()
        }

      "a result cannot be found for the given email address" in {
        AuthStub.authorise()
        EmailVerificationStub.getVerificationResult(TestData.ggCredId, Right(List()))

        testIsUpstreamErrorResponse(
          connector.getEmailVerificationResult(getResultRequest),
          NOT_FOUND
        )
      }

      "verified=true and locked=true for the given email address" in {
        AuthStub.authorise()
        EmailVerificationStub.getVerificationResult(
          TestData.ggCredId,
          Right(List(EmailResult(email.value, verified = true, locked = true)))
        )

        testIsUpstreamErrorResponse(
          connector.getEmailVerificationResult(getResultRequest),
          INTERNAL_SERVER_ERROR
        )
      }

      "verified=false and locked=false for the given email address" in {
        AuthStub.authorise()
        EmailVerificationStub.getVerificationResult(
          TestData.ggCredId,
          Right(List(EmailResult(email.value, verified = false, locked = false)))
        )

        testIsUpstreamErrorResponse(
          connector.getEmailVerificationResult(getResultRequest),
          INTERNAL_SERVER_ERROR
        )
      }

    }
    "is insensitive to email case" in {
      val emailWithUpperCase = s"Email${UUID.randomUUID().toString}@Test.Com"
      val emailCaseLowered = emailWithUpperCase.toLowerCase(Locale.UK)
      val getResultRequest = GetEmailVerificationResultRequest(Email(emailWithUpperCase))
      val id = correlationIdGenerator.readNextCorrelationId()

      AuthStub.authorise()
      EmailVerificationStub.getVerificationResult(
        TestData.ggCredId,
        Right(List(EmailResult(emailCaseLowered, verified = true, locked = false)))
      )

      val result = connector.getEmailVerificationResult(getResultRequest)
      await(result) shouldBe EmailVerificationResult.Verified

      val emailVerificationStatus = await(emailVerificationStatusRepo.findAllEntries(TestData.ggCredId))
      emailVerificationStatus.size shouldBe 1
      emailVerificationStatus.headOption.map(_._id) shouldBe Some(id.value)
      emailVerificationStatus.headOption.map(_.email.value.decryptedValue) shouldBe Some(emailWithUpperCase)
      emailVerificationStatus.headOption.map(_.numberOfPasscodeJourneysStarted.value) shouldBe Some(1)
      emailVerificationStatus.headOption.flatMap(_.verificationResult) shouldBe Some(EmailVerificationResult.Verified)

      EmailVerificationStub.verifyNoneGetVerificationStatus(TestData.ggCredId)
    }

  }

  "GET /email-verification/earliest-created-at" - {

    behave like authenticatedEndpointBehaviour(connector.getEarliestCreatedAtTime()(using _))

    "should return the earliest created at date when one can be found" in {
      AuthStub.authorise()

      val emailVerificationStatusEarlier: EmailVerificationStatus = EmailVerificationStatus(
        _id                             = UUID.randomUUID(),
        credId                          = TestData.ggCredId,
        email                           = EncryptedEmail(SensitiveString(s"email${UUID.randomUUID().toString}@test.com")),
        numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(1),
        verificationResult              = None,
        createdAt                       = TestData.frozenInstant,
        lastUpdated                     = TestData.frozenInstant
      )
      val emailVerificationStatusLater: EmailVerificationStatus = EmailVerificationStatus(
        _id                             = UUID.randomUUID(),
        credId                          = TestData.ggCredId,
        email                           = EncryptedEmail(SensitiveString(s"email${UUID.randomUUID().toString}@test.com")),
        numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(1),
        verificationResult              = None,
        createdAt                       = emailVerificationStatusEarlier.createdAt.plus(1, ChronoUnit.MINUTES),
        lastUpdated                     = TestData.frozenInstant
      )

      emailVerificationStatusRepo.upsert(emailVerificationStatusEarlier).futureValue
      emailVerificationStatusRepo.upsert(emailVerificationStatusLater).futureValue

      val result = connector.getEarliestCreatedAtTime()
      await(result).earliestCreatedAtTime.map(_.withNano(0)) shouldBe Some(
        LocalDateTime.ofInstant(TestData.frozenInstant, ZoneOffset.UTC).withNano(0)
      )
    }

    "should return None when no email statuses can be found" in {
      AuthStub.authorise()

      val result = connector.getEarliestCreatedAtTime()
      await(result).earliestCreatedAtTime shouldBe None
    }

  }

  private def authenticatedEndpointBehaviour[A](getResult: HeaderCarrier => Future[A]): Unit = {

    "must throw an error if no Authorization is found in the HeaderCarrier" in {
      val error = intercept[UpstreamErrorResponse](await(getResult(HeaderCarrier())))
      error.statusCode shouldBe UNAUTHORIZED
    }

    "must throw an error if no cred id can be found in the HeaderCarrier" in {
      AuthStub.authorise(None)

      val hcWithAuthorisation: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(TestData.authToken)))

      val error = intercept[UpstreamErrorResponse](await(getResult(hcWithAuthorisation)))
      error.statusCode shouldBe INTERNAL_SERVER_ERROR
    }

  }

}

class EmailVerificationNonLocalControllerSpec extends ItSpec {

  override def conf: Map[String, Any] = super.conf.updated("is-local", false)

  val connector = app.injector.instanceOf[PaymentsEmailVerificationConnector]

  given HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(TestData.authToken)))

  "POST /email-verification/start" - {

    val startEmailVerificationJourneyRequest = StartEmailVerificationJourneyRequest(
      "continue",
      "origin",
      "deskpro",
      "accessibility",
      "title",
      "back",
      "enter",
      Email(s"email${UUID.randomUUID().toString}@test.com"),
      "en"
    )

    "maintain the redirect url if it is relative and the environment is not local" in {
      val redirectUri: String = "/redirect"

      AuthStub.authorise()
      EmailVerificationStub.requestEmailVerification(Right(RequestEmailVerificationSuccess(redirectUri)))

      val result = connector.startEmailVerification(startEmailVerificationJourneyRequest)
      await(result) shouldBe StartEmailVerificationJourneyResponse.Success(redirectUri)

      EmailVerificationStub.verifyRequestEmailVerification(startEmailVerificationJourneyRequest, TestData.ggCredId)
    }

  }

}
