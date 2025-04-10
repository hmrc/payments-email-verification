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

package uk.gov.hmrc.paymentsemailverification.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.FiniteDuration

@Singleton
class AppConfig @Inject() (config: Configuration) extends ServicesConfig(config) {

  val appName: String = config.get[String]("appName")

  val emailVerificationUrl: String = baseUrl("email-verification")

  val emailVerificationStatusRepoTtl: FiniteDuration     = config.get[FiniteDuration]("email-verification-status.repoTtl")
  val emailVerificationStatusMaxAttemptsPerEmail: Int    = config.get[Int]("email-verification-status.maxAttemptsPerEmail")
  val emailVerificationStatusMaxUniqueEmailsAllowed: Int =
    config.get[Int]("email-verification-status.maxUniqueEmailsAllowed")

  val isLocal: Boolean                              = config.get[Boolean]("is-local")
  val emailVerificationFrontendBaseUrlLocal: String = config.get[String]("email-verification-frontend.base-url.local")

}
