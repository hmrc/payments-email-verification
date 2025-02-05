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
import play.api.libs.json.OFormat
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonEncoder}

import scala.collection.immutable

sealed trait EmailVerificationState extends EnumEntry

sealed trait EmailVerificationStateError extends EmailVerificationState

object EmailVerificationState extends Enum[EmailVerificationState] {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given OFormat[EmailVerificationState] = DerivedJson.oformat(
    DeriveJsonEncoder.gen[EmailVerificationState],
    DeriveJsonDecoder.gen[EmailVerificationState]
  )

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given OFormat[EmailVerificationStateError] = DerivedJson.oformat(
    DeriveJsonEncoder.gen[EmailVerificationStateError],
    DeriveJsonDecoder.gen[EmailVerificationStateError]
  )

  case object OkToBeVerified extends EmailVerificationState

  case object AlreadyVerified extends EmailVerificationStateError

  case object TooManyPasscodeAttempts extends EmailVerificationStateError

  case object TooManyPasscodeJourneysStarted extends EmailVerificationStateError

  case object TooManyDifferentEmailAddresses extends EmailVerificationStateError

  override val values: immutable.IndexedSeq[EmailVerificationState] = findValues

}
