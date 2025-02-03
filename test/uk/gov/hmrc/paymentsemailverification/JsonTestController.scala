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

package uk.gov.hmrc.paymentsemailverification

import julienrf.json.derived
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsError, JsObject, JsResult, JsSuccess, JsValue, Json, OFormat}
import uk.gov.hmrc.paymentsemailverification.JsonTestController.Fruit
import zio.json._

object DerivedJson {

  def oformat[A](encoder: JsonEncoder[A], decoder: JsonDecoder[A]): OFormat[A] = {
    new OFormat[A] {
      override def writes(o: A): JsObject =
        Json.parse(encoder.encodeJson(o).toString).as[JsObject]

      override def reads(json: JsValue): JsResult[A] =
        decoder
          .decodeJson(json.toString)
          .fold[JsResult[A]](e => JsError(e), JsSuccess(_))

    }
  }
}

object JsonTestController {

  sealed trait Fruit extends Product with Serializable

  object Fruit {

    final case class Banana(i: Int) extends Fruit

    case object Apple extends Fruit

  }

}

class JsonTestController extends AnyWordSpecLike with Matchers {

  val banana: Fruit = Fruit.Banana(1)
  val expectedBananaJson = Json.parse("""{ "Banana": { "i": 1  } }""".stripMargin)

  val apple: Fruit = Fruit.Apple
  val expectedAppleJson = Json.parse("""{ "Apple": { } }""".stripMargin)

  "test play-json-derived-codecs" in {
    implicit val format: OFormat[Fruit] = derived.oformat()

    Json.toJson(banana) shouldBe expectedBananaJson
    expectedBananaJson.as[Fruit] shouldBe banana

    Json.toJson(apple) shouldBe expectedAppleJson
    expectedAppleJson.as[Fruit] shouldBe apple
  }

  "test zio" in {
    @SuppressWarnings(Array("org.wartremover.warts.SeqApply"))
    implicit val format: OFormat[Fruit] = DerivedJson.oformat(
      DeriveJsonEncoder.gen[Fruit],
      DeriveJsonDecoder.gen[Fruit]
    )

    Json.toJson(banana) shouldBe expectedBananaJson
    expectedBananaJson.as[Fruit] shouldBe banana

    Json.toJson(apple) shouldBe expectedAppleJson
    expectedAppleJson.as[Fruit] shouldBe apple
  }

}
