package com.streamhub

import java.time.LocalDate

object BrowsingMetaData {


  case class QueryBrowsingData(id: String,
                               group: String,
                               transactionDate: LocalDate)

  case class queryParam(group: String, metric: String, startDate: LocalDate, endDate: LocalDate){
    require(!group.isEmpty, "group name must not be empty")
    require(!metric.isEmpty, "metric name must not be empty")
    require(startDate.isBefore(endDate), "startDate must be before endDate")
    require(startDate != null, "startDate must not be null")
    require(endDate != null, "endDate must not be null")
  }

  case class BrowsingTransactions(id: String,
                                  group: String,
                                  transactionDate: LocalDate)

  case class BrowsingHits(hits: Int)

  class BrowsingNotFoundException(group: String) extends Throwable("No browsing for " + group)

}
