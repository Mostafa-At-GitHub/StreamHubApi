import java.time.LocalDate
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, get, onComplete, parameters, pathPrefix, _}
import akka.http.scaladsl.server.Route
import com.streamhub.BrowsingMetaData.{BrowsingHits, BrowsingNotFoundException, queryParam}
import com.streamhub.Utils.{getNumberOfUniqueHitsPerGroup, retry}

import scala.util.{Failure, Success}

object TestingUtils {

  import com.streamhub.BrowsingJsonProtocol._
  var group = "broadcasters"
  var startDate = LocalDate.parse("2019-01-01")
  var endDate = LocalDate.parse("2019-06-01")
  var metric = "sh"

  lazy val reportRoutes: Route = pathPrefix("report") {
    get {
      parameters('group.as[String], 'metric.as[String], 'startDate.as(stringToLocalDate), 'endDate.as(stringToLocalDate)).as(queryParam) {
        param => {
          if (param.group == "broadcasters" && param.metric.contains("uniqueUsers")) {
            onComplete(retry[BrowsingHits](getNumberOfUniqueHitsPerGroup(param), 3)) {
              case Success(hits: BrowsingHits) =>
                complete(StatusCodes.OK, hits)
              case Failure(throwable) =>
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
