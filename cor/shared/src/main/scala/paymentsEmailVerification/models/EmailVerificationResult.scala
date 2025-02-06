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

import cats.Eq
import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.OFormat
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

import scala.collection.immutable

sealed trait EmailVerificationResult extends EnumEntry

object EmailVerificationResult extends Enum[EmailVerificationResult] {

  implicit val eq: Eq[EmailVerificationResult] = Eq.fromUniversalEquals

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given OFormat[EmailVerificationResult] = DerivedJson.oformat(
    DeriveJsonEncoder.gen[EmailVerificationResult],
    DeriveJsonDecoder.gen[EmailVerificationResult]
  )

  final case class Verified() extends EmailVerificationResult

  final case class Locked() extends EmailVerificationResult

  override val values: immutable.IndexedSeq[EmailVerificationResult] = findValues

}
