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

package paymentsEmailVerification.models.api

import paymentsEmailVerification.models.EmailVerificationStateError
import play.api.libs.json.{Format, Json}

sealed trait StartEmailVerificationJourneyResponse

object StartEmailVerificationJourneyResponse {

  final case class Success(redirectUrl: String) extends StartEmailVerificationJourneyResponse


  final case class Error(reason: EmailVerificationStateError) extends StartEmailVerificationJourneyResponse
  
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[StartEmailVerificationJourneyResponse] = Json.format[StartEmailVerificationJourneyResponse]
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[StartEmailVerificationJourneyResponse.Success] = Json.format[StartEmailVerificationJourneyResponse.Success]
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Format[StartEmailVerificationJourneyResponse.Error] = Json.format[StartEmailVerificationJourneyResponse.Error]

}
