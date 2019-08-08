import java.time.LocalDate
import TestingUtils._
import com.streamhub.BrowsingMetaData.{BrowsingHits, queryParam}
import com.streamhub.Utils._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers, ParallelTestExecution}
import scala.concurrent.duration._

class BrowsingUtilsTest extends FlatSpec with Matchers with ScalaFutures with ParallelTestExecution {
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(10.second)

  it should "Testing Get Unique Hits" in {
    val param: queryParam = queryParam(group, metric, startDate, endDate)
    whenReady(getNumberOfUniqueHitsPerGroup(param)) {
      _ should be(BrowsingHits(3))
    }

  }

  it should "Zero Hits" in {
    group = "MoustafaGroup"
    val param: queryParam = queryParam(group, metric, startDate, endDate)
    whenReady(getNumberOfUniqueHitsPerGroup(param)) {
      _ should be(BrowsingHits(0))
    }
  }


  it should "return IllegalArgumentException missing group parameters" in {
    assertThrows[IllegalArgumentException] {
      val group = ""
      queryParam(group, metric, startDate, endDate)
    }
  }

  it should "return IllegalArgumentException endDate is before startDate" in {
    assertThrows[IllegalArgumentException] {
      endDate = startDate.minusDays(1)
      queryParam(group, metric, startDate, endDate)
    }
  }
}
