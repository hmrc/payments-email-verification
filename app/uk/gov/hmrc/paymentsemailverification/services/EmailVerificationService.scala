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

package uk.gov.hmrc.paymentsemailverification.services

import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import paymentsEmailVerification.models.EmailVerificationState._
import paymentsEmailVerification.models._
import paymentsEmailVerification.models.api._
import play.api.http.Status.{CREATED, UNAUTHORIZED}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.paymentsemailverification.config.AppConfig
import uk.gov.hmrc.paymentsemailverification.connectors.EmailVerificationConnector
import uk.gov.hmrc.paymentsemailverification.models.{EmailVerificationStatus, EncryptedEmail, GGCredId}
import uk.gov.hmrc.paymentsemailverification.models.emailverification.RequestEmailVerificationRequest.EmailDetails
import uk.gov.hmrc.paymentsemailverification.models.emailverification._
import uk.gov.hmrc.paymentsemailverification.utils.Errors

import java.net.URI
import java.util.Locale
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailVerificationService @Inject() (
  appConfig:                      AppConfig,
  connector:                      EmailVerificationConnector,
  emailVerificationStatusService: EmailVerificationStatusService
)(using ExecutionContext) {

  private val maxPasscodeJourneysPerEmailAddress: Int = appConfig.emailVerificationStatusMaxAttemptsPerEmail
  private val maxNumberOfDifferentEmails: Int         = appConfig.emailVerificationStatusMaxUniqueEmailsAllowed

  def startEmailVerificationJourney(
    request:  StartEmailVerificationJourneyRequest,
    ggCredId: GGCredId
  )(using HeaderCarrier): Future[StartEmailVerificationJourneyResponse] = {
    val emailVerificationRequest = RequestEmailVerificationRequest(
      ggCredId,
      request.continueUrl,
      request.origin,
      request.deskproServiceName,
      request.accessibilityStatementUrl,
      request.pageTitle,
      request.backUrl,
      EmailDetails(
        request.email,
        request.enterEmailUrl
      ),
      request.lang
    )

    def makeEmailVerificationCall: Future[StartEmailVerificationJourneyResponse] =
      connector
        .requestEmailVerification(emailVerificationRequest)
        .map { response =>
          if (response.status === CREATED) {
            response.json
              .validate[RequestEmailVerificationSuccess]
              .fold(
                _ => Errors.throwServerErrorException("Could not parse response from email verification"),
                { success =>
                  val redirectUrl =
                    if (appConfig.isLocal && !URI.create(success.redirectUri).isAbsolute) {
                      s"${appConfig.emailVerificationFrontendBaseUrlLocal}${success.redirectUri}"
                    } else {
                      success.redirectUri
                    }
                  StartEmailVerificationJourneyResponse.Success(redirectUrl)
                }
              )
          } else {
            throw UpstreamErrorResponse(
              s"Call to request email verification came back with unexpected status ${response.status.toString}",
              response.status
            )
          }
        }
        .recover {
          case u: UpstreamErrorResponse if u.statusCode === UNAUTHORIZED =>
            StartEmailVerificationJourneyResponse.Error(TooManyPasscodeAttempts())
        }

    emailVerificationStatusService
      .updateNumberOfPasscodeJourneys(emailVerificationRequest.credId, emailVerificationRequest.email.address)
      .flatMap { _ =>
        emailVerificationStatusService.findEmailVerificationStatuses(ggCredId).flatMap {
          getState(request.email, _) match {
            case EmailVerificationState.OkToBeVerified() =>
              makeEmailVerificationCall

            case EmailVerificationState.AlreadyVerified() =>
              Future.successful(StartEmailVerificationJourneyResponse.Error(AlreadyVerified()))

            case EmailVerificationState.TooManyPasscodeAttempts() =>
              Future.successful(StartEmailVerificationJourneyResponse.Error(TooManyPasscodeAttempts()))

            case EmailVerificationState.TooManyPasscodeJourneysStarted() =>
              Future.successful(StartEmailVerificationJourneyResponse.Error(TooManyPasscodeJourneysStarted()))

            case EmailVerificationState.TooManyDifferentEmailAddresses() =>
              Future.successful(StartEmailVerificationJourneyResponse.Error(TooManyDifferentEmailAddresses()))
          }
        }
      }
  }

  def getVerificationResult(
    request:  GetEmailVerificationResultRequest,
    ggCredId: GGCredId
  )(using HeaderCarrier): Future[EmailVerificationResult] =
    connector.getVerificationStatus(ggCredId).flatMap { statusResponse =>
      statusResponse.emails
        .find(_.emailAddress.toLowerCase(Locale.UK) === request.email.value.toLowerCase(Locale.UK)) match {
        case None         =>
          Errors.throwNotFoundException("Verification result not found for email address")
        case Some(status) =>
          (status.verified, status.locked) match {
            case (true, false) =>
              emailVerificationStatusService
                .updateEmailVerificationStatusResult(ggCredId, request.email, EmailVerificationResult.Verified())
                .map(_ => EmailVerificationResult.Verified())

            case (false, true) =>
              emailVerificationStatusService
                .updateEmailVerificationStatusResult(ggCredId, request.email, EmailVerificationResult.Locked())
                .map(_ => EmailVerificationResult.Locked())

            case _ =>
              Errors.throwServerErrorException(
                s"Got unexpected combination of verified=${status.verified.toString} and " +
                  s"locked=${status.locked.toString} in email verification status response"
              )
          }
      }
    }

  private def getState(currentEmail: Email, statuses: List[EmailVerificationStatus]): EmailVerificationState = {
    val statusForCurrentEmail: Option[EmailVerificationStatus] =
      statuses.find(_.email === EncryptedEmail.fromEmail(currentEmail))

    val alreadyVerified: Boolean =
      statusForCurrentEmail.exists(_.verificationResult.contains(EmailVerificationResult.Verified()))

    val tooManyPasscodeAttempts: Boolean =
      statusForCurrentEmail.exists(_.verificationResult.contains(EmailVerificationResult.Locked()))

    val tooManyPasscodeJourneysStarted: Boolean =
      statusForCurrentEmail.exists(_.numberOfPasscodeJourneysStarted.value >= maxPasscodeJourneysPerEmailAddress)

    val tooManyDifferentEmailAddresses: Boolean =
      statuses.sizeIs >= maxNumberOfDifferentEmails

    if (alreadyVerified) EmailVerificationState.AlreadyVerified()
    else if (tooManyDifferentEmailAddresses) EmailVerificationState.TooManyDifferentEmailAddresses()
    else if (tooManyPasscodeAttempts) EmailVerificationState.TooManyPasscodeAttempts()
    else if (tooManyPasscodeJourneysStarted) EmailVerificationState.TooManyPasscodeJourneysStarted()
    else EmailVerificationState.OkToBeVerified()
  }

}
