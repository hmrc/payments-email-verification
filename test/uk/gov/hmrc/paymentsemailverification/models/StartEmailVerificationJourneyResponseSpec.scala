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

import paymentsEmailVerification.models.EmailVerificationState
import paymentsEmailVerification.models.api.StartEmailVerificationJourneyResponse
import play.api.libs.json.*
import uk.gov.hmrc.paymentsemailverification.testsupport.UnitSpec
import uk.gov.hmrc.paymentsemailverification.testsupport.Givens.{jsResultCanEqual, jsValueCanEqual}

class StartEmailVerificationJourneyResponseSpec extends UnitSpec {

  val successJson = Json.parse(
    """{
      | "Success": {
      |   "redirectUrl": "https://example.com/verify-email"
      | }
      |}""".stripMargin
  )

  val errorJson = Json.parse(
    """{
      | "Error": {
      |   "reason": {
      |     "TooManyPasscodeAttempts": {}
      |   }
      | }
      |}""".stripMargin
  )

  val successResponse: StartEmailVerificationJourneyResponse =
    StartEmailVerificationJourneyResponse.Success(redirectUrl = "https://example.com/verify-email")

  val errorResponse: StartEmailVerificationJourneyResponse =
    StartEmailVerificationJourneyResponse.Error(reason = EmailVerificationState.TooManyPasscodeAttempts)

  "StartEmailVerificationJourneyResponse JSON serialization and deserialization" - {

    "serialize each case class to the correct JSON" in {
      Json.toJson(successResponse) shouldBe successJson
      Json.toJson(errorResponse) shouldBe errorJson
    }

    "deserialize from valid JSON to the correct case class" in {
      successJson.validate[StartEmailVerificationJourneyResponse] shouldBe JsSuccess(successResponse)
      errorJson.validate[StartEmailVerificationJourneyResponse] shouldBe JsSuccess(errorResponse)
    }

    "fail to deserialize from invalid JSON" in {
      JsString("InvalidCaseClass").validate[StartEmailVerificationJourneyResponse].isError shouldBe true
      JsNumber(42).validate[StartEmailVerificationJourneyResponse].isError shouldBe true
      JsObject(Seq.empty).validate[StartEmailVerificationJourneyResponse].isError shouldBe true
    }
  }
}
