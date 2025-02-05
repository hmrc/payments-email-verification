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

import paymentsEmailVerification.models.{EmailVerificationState, EmailVerificationStateError}
import play.api.libs.json.*
import uk.gov.hmrc.paymentsemailverification.testsupport.UnitSpec

class EmailVerificationStateSpec extends UnitSpec {
  
  val okToBeVerifiedJson = Json.parse("""{ "OkToBeVerified": {} }""")
  val alreadyVerifiedJson = Json.parse("""{ "AlreadyVerified": {} }""")
  val tooManyPasscodeAttemptsJson = Json.parse("""{ "TooManyPasscodeAttempts": {} }""")
  val tooManyPasscodeJourneysStartedJson = Json.parse("""{ "TooManyPasscodeJourneysStarted": {} }""")
  val tooManyDifferentEmailAddressesJson = Json.parse("""{ "TooManyDifferentEmailAddresses": {} }""")
  
  val okToBeVerified: EmailVerificationState = EmailVerificationState.OkToBeVerified
  val alreadyVerified: EmailVerificationStateError = EmailVerificationState.AlreadyVerified
  val tooManyPasscodeAttempts: EmailVerificationStateError = EmailVerificationState.TooManyPasscodeAttempts
  val tooManyPasscodeJourneysStarted: EmailVerificationStateError = EmailVerificationState.TooManyPasscodeJourneysStarted
  val tooManyDifferentEmailAddresses: EmailVerificationStateError = EmailVerificationState.TooManyDifferentEmailAddresses

  "EmailVerificationState JSON serialization and deserialization" - {

    "serialize each case object to the correct JSON" in {
      Json.toJson(okToBeVerified) shouldBe okToBeVerifiedJson
      Json.toJson(alreadyVerified) shouldBe alreadyVerifiedJson
      Json.toJson(tooManyPasscodeAttempts) shouldBe tooManyPasscodeAttemptsJson
      Json.toJson(tooManyPasscodeJourneysStarted) shouldBe tooManyPasscodeJourneysStartedJson
      Json.toJson(tooManyDifferentEmailAddresses) shouldBe tooManyDifferentEmailAddressesJson
    }

    "deserialize from valid JSON to the correct case object" in {
      okToBeVerifiedJson.validate[EmailVerificationState] shouldBe JsSuccess(okToBeVerified)
      alreadyVerifiedJson.validate[EmailVerificationStateError] shouldBe JsSuccess(alreadyVerified)
      tooManyPasscodeAttemptsJson.validate[EmailVerificationStateError] shouldBe JsSuccess(tooManyPasscodeAttempts)
      tooManyPasscodeJourneysStartedJson.validate[EmailVerificationStateError] shouldBe JsSuccess(tooManyPasscodeJourneysStarted)
      tooManyDifferentEmailAddressesJson.validate[EmailVerificationStateError] shouldBe JsSuccess(tooManyDifferentEmailAddresses)
    }

    "perform serialization and deserialization" in {
      val serializedOkJson = Json.toJson(okToBeVerified)
      serializedOkJson.validate[EmailVerificationState] shouldBe JsSuccess(okToBeVerified)

      val serializedAlreadyVerifiedJson = Json.toJson(alreadyVerified)
      serializedAlreadyVerifiedJson.validate[EmailVerificationStateError] shouldBe JsSuccess(alreadyVerified)

      val serializedTooManyPasscodeAttemptsJson = Json.toJson(tooManyPasscodeAttempts)
      serializedTooManyPasscodeAttemptsJson.validate[EmailVerificationStateError] shouldBe JsSuccess(tooManyPasscodeAttempts)

      val serializedTooManyPasscodeJourneysStartedJson = Json.toJson(tooManyPasscodeJourneysStarted)
      serializedTooManyPasscodeJourneysStartedJson.validate[EmailVerificationStateError] shouldBe JsSuccess(tooManyPasscodeJourneysStarted)

      val serializedTooManyDifferentEmailAddressesJson = Json.toJson(tooManyDifferentEmailAddresses)
      serializedTooManyDifferentEmailAddressesJson.validate[EmailVerificationStateError] shouldBe JsSuccess(tooManyDifferentEmailAddresses)
    }

    "fail to deserialize from invalid JSON" in {
      JsString("InvalidState").validate[EmailVerificationState].isError shouldBe true
      JsNumber(123).validate[EmailVerificationState].isError shouldBe true
      JsObject(Seq.empty).validate[EmailVerificationState].isError shouldBe true
    }
  }
}

