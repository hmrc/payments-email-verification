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

package uk.gov.hmrc.paymentsemailverification.testsupport

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.scalatest._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext

trait RichMatchers
    extends Matchers,
      TryValues,
      EitherValues,
      OptionValues,
      AppendedClues,
      ScalaFutures,
      StreamlinedXml,
      Inside,
      Eventually,
      IntegrationPatience,
      JsonSyntax {

  given ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  @SuppressWarnings(Array("org.wartremover.warts.ExplicitImplicitTypes", "org.wartremover.warts.PublicInference"))
  implicit def toLoggedRequestOps(lr: LoggedRequest): Object { def getBodyAsJson: JsValue } = new {
    def getBodyAsJson: JsValue = Json.parse(lr.getBodyAsString)
  }

}
