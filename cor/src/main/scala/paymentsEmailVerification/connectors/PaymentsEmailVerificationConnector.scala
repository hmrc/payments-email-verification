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
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentsEmailVerificationConnector @Inject() (httpClient: HttpClient, servicesConfig: ServicesConfig)(
    implicit
    ec: ExecutionContext
) {

  private val baseUrl: String = servicesConfig.baseUrl("payments-email-verification")

  private val startEmailVerificationUrl = s"$baseUrl/payments-email-verification/start"

  private val getEmailVerificationResultUrl = s"$baseUrl/payments-email-verification/status"

  private val getEarliestCreatedAtTime = s"$baseUrl/payments-email-verification/earliest-created-at"

  def startEmailVerification(request: StartEmailVerificationJourneyRequest)(implicit hc: HeaderCarrier): Future[StartEmailVerificationJourneyResponse] =
    httpClient.POST[StartEmailVerificationJourneyRequest, StartEmailVerificationJourneyResponse](
      startEmailVerificationUrl,
      request
    )

  def getEmailVerificationResult(request: GetEmailVerificationResultRequest)(implicit hc: HeaderCarrier): Future[EmailVerificationResult] =
    httpClient.POST[GetEmailVerificationResultRequest, EmailVerificationResult](
      getEmailVerificationResultUrl,
      request
    )

  def getEarliestCreatedAtTime()(implicit hc: HeaderCarrier): Future[GetEarliestCreatedAtTimeResponse] =
    httpClient.GET[GetEarliestCreatedAtTimeResponse](getEarliestCreatedAtTime)

}
