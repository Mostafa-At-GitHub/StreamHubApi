package com.streamhub
import java.sql.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import Utils._
import java.time.{LocalDate => JavaLocalDate}
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshaller
import com.streamhub.BrowsingMetaData.QueryBrowsingData
import com.streamhub.BrowsingMetaData.{BrowsingHits, BrowsingNotFoundException, queryParam}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, _}
import scala.util.{Failure, Success, Try}

trait BrowsingController  {

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
  import BrowsingJsonProtocol._

  /**
   * Report Routes for the GET REST endpoints for the browsing endpoints.
   */
  lazy val reportRoutes: Route = pathPrefix("report") {
    get {
      parameters('group.as[String], 'metric.as[String], 'startDate.as(stringToLocalDate), 'endDate.as(stringToLocalDate)).as(queryParam) {
        param =>
        {
          if (param.group == "broadcasters" && param.metric.contains("uniqueUsers") ) {
            onComplete(retry[BrowsingHits](getNumberOfUniqueHitsPerGroup(param),3)) {
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

