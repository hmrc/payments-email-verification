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
import paymentsEmailVerification.models.api.GetEarliestCreatedAtTimeResponse
import paymentsEmailVerification.models.{Email, EmailVerificationResult}
import uk.gov.hmrc.paymentsemailverification.models.{EmailVerificationStatus, EncryptedEmail, GGCredId}
import uk.gov.hmrc.paymentsemailverification.repositories.EmailVerificationStatusRepo

import java.time.{Clock, Instant, LocalDateTime, ZoneOffset}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailVerificationStatusService @Inject() (
    emailVerificationStatusRepo: EmailVerificationStatusRepo,
    correlationIdGenerator:      CorrelationIdGenerator,
    clock:                       Clock
)(using ExecutionContext) {

  /*
   * return list of EmailVerificationStatus associated with given credId
   */
  def findEmailVerificationStatuses(ggCredId: GGCredId): Future[List[EmailVerificationStatus]] =
    find(ggCredId)

  given Ordering[LocalDateTime] = _ compareTo _

  def findEarliestCreatedAt(ggCredId: GGCredId): Future[GetEarliestCreatedAtTimeResponse] = {
    find(ggCredId).map { (statuses: List[EmailVerificationStatus]) =>
      val result = statuses
        .map(status => LocalDateTime.ofInstant(status.createdAt, ZoneOffset.UTC))
        .minOption

      GetEarliestCreatedAtTimeResponse(result)
    }
  }

  /*
   * increment the verification attempts for EmailVerificationStatus entry that matches given credId and email,
   * or create one if it doesn't exist
   */
  def updateNumberOfPasscodeJourneys(credId: GGCredId, email: Email): Future[Unit] = {
    val encryptedEmail = EncryptedEmail.fromEmail(email)

    findEmailVerificationStatuses(credId).flatMap {
      _.find(_.email === encryptedEmail)
        .fold{
          upsert(EmailVerificationStatus(correlationIdGenerator.nextCorrelationId(), credId, encryptedEmail, None, clock))
        } {
          emailVerificationStatus =>
            upsert(
              emailVerificationStatus.copy(
                numberOfPasscodeJourneysStarted = emailVerificationStatus.numberOfPasscodeJourneysStarted.increment,
                lastUpdated                     = Instant.now(clock)
              )
            )
        }
    }
  }

  /*
   * update the emailVerificationResult for EmailVerificationStatus entry that matches given credId and email,
   * or create one if it doesn't exist
   */
  def updateEmailVerificationStatusResult(credId: GGCredId, email: Email, emailVerificationResult: EmailVerificationResult): Future[Unit] = {
    val encryptedEmail = EncryptedEmail.fromEmail(email)

    findEmailVerificationStatuses(credId).flatMap {
      _.find(_.email === encryptedEmail)
        .fold {
          upsert(EmailVerificationStatus(correlationIdGenerator.nextCorrelationId(), credId, encryptedEmail, Some(emailVerificationResult), clock))
        } {
          emailVerificationStatus =>
            upsert(emailVerificationStatus.copy(
              verificationResult = Some(emailVerificationResult),
              lastUpdated        = Instant.now(clock)
            ))
        }
    }
  }

  private def find(ggCredId: GGCredId): Future[List[EmailVerificationStatus]] =
    emailVerificationStatusRepo.findAllEntries(ggCredId)

  private def upsert(emailVerificationStatus: EmailVerificationStatus): Future[Unit] =
    emailVerificationStatusRepo.update(emailVerificationStatus)

}
