package com.streamhub
import java.sql.Date
import java.time.{LocalDate => JavaLocalDate}
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshaller
import com.streamhub.BrowsingRepository.QueryBrowsingData
import com.streamhub.BrowsingRepository.{BrowsingHits, BrowsingNotFoundException, queryParam}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, _}
import scala.util.{Failure, Success, Try}

object BrowsingController {

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
  }

}

trait BrowsingController extends BrowsingRepository {

  /**
   * The Actor system to be used by the Future Context.
   *
   * @return
   */
  implicit def actorSystem: ActorSystem

  /**
   * Logging using the actor system.
   */
  lazy val logger = Logging(actorSystem, classOf[BrowsingController])
  import BrowsingController.BrowsingJsonProtocol._


  implicit val stringToLocalDate: Unmarshaller[String, JavaLocalDate] =
    Unmarshaller.strict[String, JavaLocalDate] { string ⇒
      import java.time.LocalDate
      import java.time.format.DateTimeFormatter
      var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val date = LocalDate.parse(string, formatter)
      date
    }

  /**
   * Report Routes for the GET/POST/Other REST endpoints for the browsing endpoints.
   */
    //https://stackoverflow.com/questions/36338447/how-to-retry-failed-unmarshalling-of-a-stream-of-akka-http-requests
  lazy val reportRoutes: Route = pathPrefix("report") {
    get {
      parameters('group.as[String], 'metric.as[String], 'startDate.as(stringToLocalDate), 'endDate.as(stringToLocalDate)).as(queryParam) {
        param =>
        {
          if (param.group == "broadcasters" && param.metric.contains("uniqueUsers") ) {
            onComplete(retry[BrowsingHits](getNumberOfUniqueHitsPerGroup(param.group, param.startDate, param.endDate),3)) {
              case Success(hits: BrowsingHits) =>
                logger.info(s"Got the browsing records given the browsing id ${param.group}")
                complete(StatusCodes.OK, hits)
              case Failure(throwable) =>
                logger.error(s"Failed to get the browsing record given the browsing id ${param.group}")
                throwable match {
                  case e: BrowsingNotFoundException => complete(StatusCodes.Forbidden, "No browsing found")
                  case _ => complete(StatusCodes.InternalServerError, "Failed to get the browsing.")
                }
            }
          }
          else {
            complete(StatusCodes.Forbidden, "Failed to get the browsing.")
          }
        }
      }
    }
  }
}

