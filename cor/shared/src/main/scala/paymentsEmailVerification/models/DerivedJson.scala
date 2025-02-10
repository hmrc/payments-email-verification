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

import io.bullet.borer.{Json => BorerJson, *}

import play.api.libs.json.{JsError, JsObject, JsResult, JsSuccess, JsValue, Json, OFormat}

object DerivedJson {

  object Borer {

    def oformat[A](using codec: Codec[A]): OFormat[A] =
      new OFormat[A] {
        override def writes(o: A): JsObject =
          Json.parse(BorerJson.encode(o).toUtf8String).as[JsObject]

        override def reads(json: JsValue): JsResult[A] =
          BorerJson
            .decode(json.toString.getBytes("UTF-8"))
            .to[A]
            .valueEither
            .fold[JsResult[A]](e => JsError(e.getMessage), JsSuccess(_))
      }
  }

}
