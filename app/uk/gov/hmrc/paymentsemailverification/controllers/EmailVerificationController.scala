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

package uk.gov.hmrc.paymentsemailverification.controllers

import com.google.inject.{Inject, Singleton}
import paymentsEmailVerification.models.api.{GetEmailVerificationResultRequest, StartEmailVerificationJourneyRequest}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request}
import uk.gov.hmrc.paymentsemailverification.actions.Actions
import uk.gov.hmrc.paymentsemailverification.crypto.CryptoFormat
import uk.gov.hmrc.paymentsemailverification.services.{EmailVerificationService, EmailVerificationStatusService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class EmailVerificationController @Inject() (
  actions:                        Actions,
  emailVerificationService:       EmailVerificationService,
  emailVerificationStatusService: EmailVerificationStatusService,
  cc:                             ControllerComponents
)(using ExecutionContext)
    extends BackendController(cc) {

  given CryptoFormat = CryptoFormat.NoOpCryptoFormat

  val startEmailVerificationJourney: Action[StartEmailVerificationJourneyRequest] =
    actions.authenticatedAction(parse.json[StartEmailVerificationJourneyRequest]).async { request =>
      given Request[StartEmailVerificationJourneyRequest] = request

      emailVerificationService
        .startEmailVerificationJourney(request.body, request.ggCredId)
        .map(result => Ok(Json.toJson(result)))
    }

  val getEmailVerificationResult: Action[GetEmailVerificationResultRequest] =
    actions.authenticatedAction(parse.json[GetEmailVerificationResultRequest]).async { request =>
      given Request[GetEmailVerificationResultRequest] = request

      emailVerificationService
        .getVerificationResult(request.body, request.ggCredId)
        .map(result => Ok(Json.toJson(result)))
    }

  val getEarliestCreatedAt: Action[AnyContent] =
    actions.authenticatedAction.async { request =>
      emailVerificationStatusService
        .findEarliestCreatedAt(request.ggCredId)
        .map(result => Ok(Json.toJson(result)))
    }

}
