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
import play.api.libs.json.Json
import uk.gov.hmrc.paymentsemailverification.testsupport.UnitSpec

class EmailVerificationResultSpec extends UnitSpec {

  val resultJson = Json.parse(
    """{
      | "Verified": {}
      |}""".stripMargin)

  val model = EmailVerificationResult.Verified

  "model should parse to json" in {

    Json.toJson(model) shouldBe resultJson
  }

}
