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
import io.bullet.borer.derivation.MapBasedCodecs.*
import scala.collection.immutable

sealed trait EmailVerificationResult extends EnumEntry derives CanEqual

object EmailVerificationResult extends Enum[EmailVerificationResult] {

  implicit val eq: Eq[EmailVerificationResult] = Eq.fromUniversalEquals

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given OFormat[EmailVerificationResult] = DerivedJson.Borer.oformat(using
    deriveAllCodecs[EmailVerificationResult]
  )

  case object Verified extends EmailVerificationResult

  case object Locked extends EmailVerificationResult

  override val values: immutable.IndexedSeq[EmailVerificationResult] = findValues

}
