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
import play.api.libs.json._

import scala.collection.immutable

sealed trait EmailVerificationResult extends EnumEntry

object EmailVerificationResult extends Enum[EmailVerificationResult] {

  implicit val eq: Eq[EmailVerificationResult] = Eq.fromUniversalEquals

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[EmailVerificationResult] = Json.format[EmailVerificationResult]

  case object Verified extends EmailVerificationResult

  implicit val verifiedFormat: OFormat[Verified.type] = OFormat[Verified.type](Reads[Verified.type] {
    case JsObject(_) => JsSuccess(Verified)
    case _           => JsError("Empty object expected")
  }, OWrites[Verified.type] { _ =>
    Json.obj()
  })

  case object Locked extends EmailVerificationResult

  implicit val lockedFormat: OFormat[Locked.type] = OFormat[Locked.type](Reads[Locked.type] {
    case JsObject(_) => JsSuccess(Locked)
    case _ => JsError("Empty object expected")
  }, OWrites[Locked.type] { _ =>
    Json.obj()
  })


  override val values: immutable.IndexedSeq[EmailVerificationResult] = findValues

}

