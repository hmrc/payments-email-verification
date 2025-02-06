/*
 * Copyright 2025 HM Revenue & Customs
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

import paymentsEmailVerification.models.EmailVerificationResult
import play.api.libs.json.{JsNumber, JsObject, JsString, JsSuccess, Json}
import uk.gov.hmrc.paymentsemailverification.testsupport.UnitSpec
import uk.gov.hmrc.paymentsemailverification.testsupport.Givens.{jsResultCanEqual, jsValueCanEqual}

class EmailVerificationResultSpec extends UnitSpec {

  val resultJsonVerified = Json.parse("""{
      | "Verified": {}
      |}""".stripMargin)

  val resultJsonLocked = Json.parse("""{
      | "Locked": {}
      |}""".stripMargin)

  val modelVerified: EmailVerificationResult = EmailVerificationResult.Verified()
  val modelLocked: EmailVerificationResult   = EmailVerificationResult.Locked()

  "EmailVerificationResult JSON serialization and deserialization" - {

    "serialize each case object to the correct JSON" in {
      Json.toJson(modelVerified) shouldBe resultJsonVerified
      Json.toJson(modelLocked) shouldBe resultJsonLocked
    }

    "deserialize from valid JSON to the correct case object" in {
      resultJsonVerified.validate[EmailVerificationResult] shouldBe JsSuccess(modelVerified)
      resultJsonLocked.validate[EmailVerificationResult] shouldBe JsSuccess(modelLocked)
    }

    "fail to deserialize from invalid JSON" in {
      JsString("Invalid").validate[EmailVerificationResult].isError shouldBe true
      JsNumber(123).validate[EmailVerificationResult].isError shouldBe true
      JsObject(Seq.empty).validate[EmailVerificationResult].isError shouldBe true
    }
  }

}
