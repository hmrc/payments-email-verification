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

package uk.gov.hmrc.paymentsemailverification.models

import paymentsEmailVerification.models.{EmailVerificationResult, NumberOfPasscodeJourneysStarted}
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.paymentsemailverification.crypto.CryptoFormat

import java.time.{Clock, Instant}
import java.util.UUID

final case class EmailVerificationStatus(
  _id:                             UUID,
  credId:                          GGCredId,
  email:                           EncryptedEmail,
  numberOfPasscodeJourneysStarted: NumberOfPasscodeJourneysStarted,
  verificationResult:              Option[EmailVerificationResult],
  createdAt:                       Instant,
  lastUpdated:                     Instant
)

object EmailVerificationStatus {

  given Format[Instant] = MongoJavatimeFormats.instantFormat

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given format(using cryptoFormat: CryptoFormat): OFormat[EmailVerificationStatus] =
    Json.format[EmailVerificationStatus]

  def apply(
    correlationId:      CorrelationId,
    credId:             GGCredId,
    email:              EncryptedEmail,
    verificationResult: Option[EmailVerificationResult],
    clock:              Clock
  ): EmailVerificationStatus =
    EmailVerificationStatus(
      correlationId.value,
      credId,
      email,
      NumberOfPasscodeJourneysStarted(1),
      verificationResult,
      Instant.now(clock),
      Instant.now(clock)
    )

}
