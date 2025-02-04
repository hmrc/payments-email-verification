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

package paymentsEmailVerification.models

import play.api.libs.json.*
import zio.json.*


object DerivedJson {

  def oformat[A](encoder: JsonEncoder[A], decoder: JsonDecoder[A]): OFormat[A] = {
    new OFormat[A] {
      override def writes(o: A): JsObject =
        Json.parse(encoder.encodeJson(o).toString) match
          case j:JsObject => j
          case s:JsString => JsObject(Map(s.value -> JsObject.empty))
          case e => throw new Exception(s"Unexpected json type: ${e.getClass.getSimpleName}")

      override def reads(json: JsValue): JsResult[A] =
        decoder
          .decodeJson(json.toString)
          .fold[JsResult[A]](e => JsError(e), JsSuccess(_))

    }
  }
}



