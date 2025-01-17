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

package paymentsEmailVerification.models

import enumeratum.{Enum, EnumEntry}
import paymentsEmailVerification.models.EmailVerificationState.OkToBeVerified
import play.api.libs.json.{Format, Json, OFormat}

import scala.collection.immutable

sealed trait EmailVerificationState extends EnumEntry

sealed trait EmailVerificationStateError extends EmailVerificationState

object EmailVerificationState extends Enum[EmailVerificationState] {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[EmailVerificationState] = Json.format[EmailVerificationState]

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[EmailVerificationStateError] = Json.format[EmailVerificationStateError]

  final case class OkToBeVerified() extends EmailVerificationState

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[OkToBeVerified] = Json.format[OkToBeVerified]

  final case class AlreadyVerified() extends EmailVerificationStateError

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[AlreadyVerified] = Json.format[AlreadyVerified]

  final case class TooManyPasscodeAttempts() extends EmailVerificationStateError

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[TooManyPasscodeAttempts] = Json.format[TooManyPasscodeAttempts]

  final case class TooManyPasscodeJourneysStarted() extends EmailVerificationStateError

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[TooManyPasscodeJourneysStarted] = Json.format[TooManyPasscodeJourneysStarted]

  final case class TooManyDifferentEmailAddresses() extends EmailVerificationStateError

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[TooManyDifferentEmailAddresses] = Json.format[TooManyDifferentEmailAddresses]

  override val values: immutable.IndexedSeq[EmailVerificationState] = findValues

}
