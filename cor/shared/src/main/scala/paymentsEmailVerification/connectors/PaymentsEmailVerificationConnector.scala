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

package paymentsEmailVerification.connectors

import com.google.inject.{Inject, Singleton}
import paymentsEmailVerification.models.EmailVerificationResult
import paymentsEmailVerification.models.api.{GetEarliestCreatedAtTimeResponse, GetEmailVerificationResultRequest, StartEmailVerificationJourneyRequest, StartEmailVerificationJourneyResponse}
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentsEmailVerificationConnector @Inject() (httpClient: HttpClientV2, servicesConfig: ServicesConfig)(using
  ExecutionContext
) {

  private val baseUrl: String = servicesConfig.baseUrl("payments-email-verification")

  private val startEmailVerificationUrl = s"$baseUrl/payments-email-verification/start"

  private val getEmailVerificationResultUrl = s"$baseUrl/payments-email-verification/status"

  private val getEarliestCreatedAtTime = s"$baseUrl/payments-email-verification/earliest-created-at"

  def startEmailVerification(
    request: StartEmailVerificationJourneyRequest
  )(using HeaderCarrier): Future[StartEmailVerificationJourneyResponse] =
    httpClient
      .post(url"$startEmailVerificationUrl")
      .withBody(Json.toJson(request))
      .execute[StartEmailVerificationJourneyResponse]

  def getEmailVerificationResult(request: GetEmailVerificationResultRequest)(using
    HeaderCarrier
  ): Future[EmailVerificationResult] =
    httpClient.post(url"$getEmailVerificationResultUrl").withBody(Json.toJson(request)).execute[EmailVerificationResult]

  def getEarliestCreatedAtTime()(using HeaderCarrier): Future[GetEarliestCreatedAtTimeResponse] =
    httpClient.get(url"$getEarliestCreatedAtTime").execute[GetEarliestCreatedAtTimeResponse]

}
