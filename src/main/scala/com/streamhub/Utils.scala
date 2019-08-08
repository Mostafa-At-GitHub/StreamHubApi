package com.streamhub

import java.time.LocalDate
import akka.pattern.after
import com.streamhub.BrowsingMetaData.{BrowsingHits, BrowsingNotFoundException, BrowsingTransactions, queryParam}
import com.streamhub.RepositoryContext.{scheduler, _}
import scala.concurrent.Future
import scala.concurrent.duration._
import BrowsingRepositoryDB.BrowsingHistDB

object Utils {

  /**
   * Fetch the browsing records with a mocked delay to synthesize transaction delays.
   */
  private def fetchDBWithDelay(): Future[Seq[BrowsingTransactions]] = {
    after(1.seconds, scheduler) {
      Future {
        BrowsingHistDB
      }
    }
  }

  /**
   * heavy query get the number of unique hits
   *
   * @param param
   * @return
   */
  def getNumberOfUniqueHitsPerGroup(param:queryParam): Future[BrowsingHits] = {
    fetchDBWithDelay().map {
      db => BrowsingHits(
        db.filter(x=> {x.group == param.group && x.transactionDate.isAfter(param.startDate) && x.transactionDate.isBefore(param.endDate)    } )
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
