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

package uk.gov.hmrc.paymentsemailverification.connectors

import com.google.inject.{Inject, Singleton}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.paymentsemailverification.config.AppConfig
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.paymentsemailverification.models.GGCredId
import uk.gov.hmrc.paymentsemailverification.models.emailverification.{EmailVerificationResultResponse, RequestEmailVerificationRequest}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailVerificationConnector @Inject() (appConfig: AppConfig, httpClient: HttpClientV2)(using ExecutionContext) {

  private val requestVerificationUrl: String = appConfig.emailVerificationUrl + "/email-verification/verify-email"

  private def getVerificationStatusUrl(ggCredId: GGCredId): String =
    appConfig.emailVerificationUrl + s"/email-verification/verification-status/${ggCredId.value}"

  def requestEmailVerification(emailVerificationRequest: RequestEmailVerificationRequest)(using
    HeaderCarrier
  ): Future[HttpResponse] =
    httpClient.post(url"$requestVerificationUrl").withBody(Json.toJson(emailVerificationRequest)).execute[HttpResponse]

  def getVerificationStatus(ggCredId: GGCredId)(using HeaderCarrier): Future[EmailVerificationResultResponse] =
    httpClient.get(url"${getVerificationStatusUrl(ggCredId)}").execute[EmailVerificationResultResponse]

}
