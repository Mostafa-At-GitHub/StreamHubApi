import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers, ParallelTestExecution}
import scala.concurrent.duration._
import TestingUtils._
class BrowsingControllerTest extends FlatSpec with ScalaFutures with ScalatestRouteTest with Matchers with ParallelTestExecution {

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(new DurationInt(10).second)

  it should "return number of hits (GET /users)" in {
    // note that there's no need for the host part in the uri:
    val request = HttpRequest(uri = "/report?group=broadcasters&metric=sh:program:uniqueUsers&startDate=2019-01-01&endDate=2019-06-01")

    request ~> reportRoutes ~> check {
      status should ===(StatusCodes.OK)

      // we expect the response to be json:
      contentType should ===(ContentTypes.`application/json`)

      // and no entries should be in the list:
      entityAs[String] should ===("""{"hits":3}""")
    }
  }

  it should "return Forbidden 403 " in {
    // note that there's no need for the host part in the uri:
    val request = HttpRequest(uri = "/report?group=aggregators&metric=sh:program:uniqueUsers&startDate=2019-01-01&endDate=2019-06-01")
    request ~> reportRoutes ~> check {
      status should ===(StatusCodes.Forbidden)
    }
  }

  it should "return BadRequest in case missing param" in {
    // note that there's no need for the host part in the uri:
    val request = HttpRequest(uri = "/report?group=&metric=sh:program:uniqueUsers&startDate=2019-01-01&endDate=2019-06-01")
    request ~> Route.seal(reportRoutes) ~> check {
      status should ===(StatusCodes.BadRequest)
    }
  }
}
