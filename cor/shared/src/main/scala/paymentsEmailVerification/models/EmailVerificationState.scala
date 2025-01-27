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
import play.api.libs.json.{Format, Json, OFormat}

import scala.collection.immutable

sealed trait EmailVerificationState extends EnumEntry

sealed trait EmailVerificationStateError extends EmailVerificationState

object EmailVerificationState extends Enum[EmailVerificationState] {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[EmailVerificationState] = Json.format[EmailVerificationState]

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[EmailVerificationStateError] = Json.format[EmailVerificationStateError]

  case object OkToBeVerified extends EmailVerificationState

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[OkToBeVerified.type] = Json.format[OkToBeVerified.type]

  case object AlreadyVerified extends EmailVerificationStateError

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[AlreadyVerified.type] = Json.format[AlreadyVerified.type]

  case object TooManyPasscodeAttempts extends EmailVerificationStateError

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[TooManyPasscodeAttempts.type] = Json.format[TooManyPasscodeAttempts.type]

  case object TooManyPasscodeJourneysStarted extends EmailVerificationStateError

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[TooManyPasscodeJourneysStarted.type] = Json.format[TooManyPasscodeJourneysStarted.type]

  case object TooManyDifferentEmailAddresses extends EmailVerificationStateError

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[TooManyDifferentEmailAddresses.type] = Json.format[TooManyDifferentEmailAddresses.type]

  override val values: immutable.IndexedSeq[EmailVerificationState] = findValues

}
