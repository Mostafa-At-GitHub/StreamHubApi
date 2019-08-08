package com.streamhub

import java.time.format.DateTimeFormatter
import java.time.{LocalDate => JavaLocalDate}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.unmarshalling.Unmarshaller
import com.streamhub.BrowsingMetaData.{BrowsingHits, QueryBrowsingData}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, deserializationError}
import scala.util.{Failure, Success, Try}

object BrowsingJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val JavaLocalDateFormat: JsonFormat[JavaLocalDate] =
    new JsonFormat[JavaLocalDate] {
      override def write(obj: JavaLocalDate): JsValue = JsString(obj.toString)

      override def read(json: JsValue): JavaLocalDate = json
      match {
        case JsString(s) => Try(JavaLocalDate.parse(s))
        match {
          case Success(result) => result
          case Failure(exception) =>
            deserializationError(s"could not parse $s as Java LocalDate", exception)
        }
        case notAJsString =>
          deserializationError(s"expected a String but got a $notAJsString")
      }
    }
  implicit val browsingHitsFormat = jsonFormat1(BrowsingHits.apply)
  implicit val browsingQueryFormat = jsonFormat3(QueryBrowsingData.apply)

  implicit val stringToLocalDate: Unmarshaller[String, JavaLocalDate] =
    Unmarshaller.strict[String, JavaLocalDate] { string ⇒
      JavaLocalDate.parse(string, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

}
