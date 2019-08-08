import java.time.LocalDate

import com.streamhub.BrowsingMetaData.{BrowsingHits, queryParam}
import com.streamhub.Utils._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, FunSuite, Matchers, ParallelTestExecution}
import scala.concurrent.duration._

class BrowsingControllerTest extends FlatSpec  with Matchers with ScalaFutures with ParallelTestExecution {

  var group = ""
  var startDate = LocalDate.parse("2019-01-01")
  var endDate = LocalDate.parse("2019-06-01")
  var metric = "sh"

  it should "Testing Get Unique Hits" in {
    implicit val patienceConfig: PatienceConfig = PatienceConfig(10.second)
    val param: queryParam = queryParam(group,metric,startDate,endDate)
    whenReady(getNumberOfUniqueHitsPerGroup(param)) { _ should be(BrowsingHits(3))}

  }

  it should "Zero Hits" in {
    implicit val patienceConfig: PatienceConfig = PatienceConfig(10.second)
     group = "MoustafaGroup"
    val param: queryParam = queryParam(group,metric,startDate,endDate)
    whenReady(getNumberOfUniqueHitsPerGroup(param)) { _ should be(BrowsingHits(0))}
  }


  it should "return IllegalArgumentException missing group parameters" in {
    implicit val patienceConfig: PatienceConfig = PatienceConfig(10.second)
    assertThrows[IllegalArgumentException] {
      val group = ""
      queryParam(group,metric,startDate,endDate)
    }
  }

  it should "return IllegalArgumentException endDate is before startDate" in {
    implicit val patienceConfig: PatienceConfig = PatienceConfig(10.second)
    assertThrows[IllegalArgumentException] {
      endDate = startDate.minusDays(1)
      queryParam(group,metric,startDate,endDate)
    }
  }
}
