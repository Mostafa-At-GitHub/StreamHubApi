package com.streamhub

import BrowsingRepository._
import RepositoryContext._
import akka.pattern.after
import scala.concurrent.duration._
import java.sql.Date
import java.time.LocalDate
import akka.stream.scaladsl.Source
import java.time.{LocalDate => JavaLocalDate}
import scala.concurrent.Future

object BrowsingRepository {


  case class QueryBrowsingData(id: String,
                               group: String,
                               transactionDate: JavaLocalDate)

  case class queryParam(group: String, metric: String, startDate: LocalDate, endDate: LocalDate){
    require(!group.isEmpty, "group name must not be empty")
    require(!metric.isEmpty, "group name must not be empty")
    require(startDate.isBefore(endDate), "startDate must be before endDate")
    require(startDate != null, "startDate must not be null")
    require(endDate != null, "endDate must not be null")
  }

  case class BrowsingTransactions(id: String,
                                  group: String,
                                  transactionDate: LocalDate)

  case class BrowsingHits(hits: Int)

  val BrowsingHistDB: Seq[BrowsingTransactions] = Seq(
    BrowsingTransactions("100", "broadcasters", LocalDate.parse("2019-01-02")),
    BrowsingTransactions("100", "broadcasters", LocalDate.parse("2019-02-01")),
    BrowsingTransactions("100", "broadcasters", LocalDate.parse("2019-03-01")),
    BrowsingTransactions("105", "broadcasters", LocalDate.parse("2019-01-03")),
    BrowsingTransactions("106", "broadcasters", LocalDate.parse("2019-01-02")),
    BrowsingTransactions("102", "ad-agencies", LocalDate.parse("2019-01-01")),
    BrowsingTransactions("103", "ad-advertisers", LocalDate.parse("2019-01-01")),
    BrowsingTransactions("104", "aggregators", LocalDate.parse("2019-01-01")),
  )

  class BrowsingNotFoundException(group: String) extends Throwable("No browsing for " + group)

}

trait BrowsingRepository {

  /**
   * Fetch the employee records with a mocked delay to synthesize transaction delays.
   */
  private def fetchDBWithDelay(): Future[Seq[BrowsingTransactions]] = {
    val randomDuration = (Math.random() * 5 + 3).toInt.seconds
    after(randomDuration, scheduler) {
      Future {
        BrowsingHistDB
      }
    }
  }

  /**
   * heavy query get the number of unique hits
   *
   * @param group
   * @return
   */
  def getNumberOfUniqueHitsPerGroup(group: String,startDate:LocalDate,endDate:LocalDate): Future[BrowsingHits] = {
    fetchDBWithDelay().map {
      db => BrowsingHits(
        db.filter(x=> {x.group == group && x.transactionDate.isAfter(startDate) && x.transactionDate.isBefore(endDate)    } )
          .map(_.id).toSet.size)
    }
  }



  /**
   * heavy query get the number of unique hits
   *
   * @param f
   * @param c
   * @return Future[T]
   */
  def retry[T](f: => Future[T], c: Int): Future[T] =
    f.recoverWith {
      // you may want to only handle certain exceptions here...
      case ex: Exception
        if c > 0 =>
        println(s"failed - will retry ${c - 1} more times")
        retry(f,  c - 1)
      case failures
        if c < 0 =>
        println(s"failed -")
        sendEmail
        Future.failed(failures)
    }


  private def sendEmail(): Unit ={println("send email")}

  //micro services for the heavy queries

  /**
   * Get the browsing details given group.
   *
   * @param group
   * @return
   */
  def getBrowsingById(group: String): Future[BrowsingTransactions] = fetchDBWithDelay().map { db =>
    val data = db.filter(_.group == group)
    if (data.isEmpty)
      throw new BrowsingNotFoundException(group)
    else
      data.reduceLeft(getMemberLatestTxn)
  }

  private def getMemberLatestTxn(e1: BrowsingTransactions, e2: BrowsingTransactions): BrowsingTransactions =
    if (e1.transactionDate.isAfter(e2.transactionDate)) e1 else e2

  /**
   * Query the employee repository with the given query condition.
   *
   * @param id
   * @param groupName
   * @param transactionDate
   * @return
   */
  def queryBrowsingDB(id: String, groupName: String, transactionDate: LocalDate): Future[Seq[BrowsingTransactions]] = {
    fetchDBWithDelay().map { db =>
      db.filter { elem =>
        elem.id == id && elem.group == groupName && elem.transactionDate == transactionDate
      }
    }
  }
}
