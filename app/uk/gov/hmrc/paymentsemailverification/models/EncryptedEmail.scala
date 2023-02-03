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

package uk.gov.hmrc.paymentsemailverification.models

import cats.Eq
import paymentsEmailVerification.models.Email
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.paymentsemailverification.crypto

final case class EncryptedEmail(value: SensitiveString) extends AnyVal

object EncryptedEmail {

  implicit val eq: Eq[EncryptedEmail] = Eq.fromUniversalEquals

  implicit def format(implicit cryptoFormat: crypto.CryptoFormat): Format[EncryptedEmail] = {
    implicit val sensitiveStringFormat: Format[SensitiveString] = crypto.sensitiveStringFormat(cryptoFormat)
    Json.valueFormat
  }

  def fromEmail(email: Email): EncryptedEmail = EncryptedEmail(SensitiveString(email.value))

}
