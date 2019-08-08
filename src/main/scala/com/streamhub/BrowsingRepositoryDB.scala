package com.streamhub

import java.time.LocalDate

import com.streamhub.BrowsingMetaData.BrowsingTransactions

object BrowsingRepositoryDB {
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

}
