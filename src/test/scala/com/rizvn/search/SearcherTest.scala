package com.rizvn.search

import org.junit.Test
import org.junit.Assert._
import org.apache.commons.dbcp.BasicDataSource
import org.skife.jdbi.v2.DBI
import java.util.Date
import java.util

/**
 * Created by riz on 18/04/2014.
 */
class SearcherTest {

  @Test
  def testBuildSqlQuery(){
    //searcher with mocked method
    val searcher = new Searcher()
    val query = searcher.buildSqlQuery("hello world this is a sentence")
    println(query)
    assertNotNull(query)
  }

  @Test
  def performanceTest() = {

    for(i <- 1 to 5) {
      val queryToTest =
        """SELECT w0.urlid, w0.location, w1.location, w2.location, w3.location, w4.location, w5.location
         FROM  WORDLOCATION w0, WORDLOCATION w1, WORDLOCATION w2, WORDLOCATION w3, WORDLOCATION w4, WORDLOCATION w5
         WHERE  w0.wordId=1953  AND  w1.wordId=688  AND
         w0.urlid = w1.urlid  AND  w2.wordId=215  AND
         w0.urlid = w2.urlid  AND  w3.wordId=19467
         AND  w0.urlid = w3.urlid  AND  w4.wordId=1027
         AND  w0.urlid = w4.urlid  AND  w5.wordId=16
         AND  w0.urlid = w5.urlid """

      val database = Database(driver = "com.mysql.jdbc.Driver", url = "jdbc:mysql://localhost:8889/search_engine", user = "root", pass = "root")

      val start = new Date()
      database.withHandle(h =>
        h.select(queryToTest)
      )
      val end = new Date()
      val jdbitime = end.getTime() - start.getTime()


      //run with jdbi
      val ds = new BasicDataSource()
      ds.setDriverClassName("com.mysql.jdbc.Driver")
      ds.setUrl("jdbc:mysql://localhost:8889/search_engine")
      ds.setInitialSize(20)
      ds.setUsername("root")
      ds.setPassword("root")

      val start2 = new Date()
      val stmt = ds.getConnection().createStatement()
      val rs = stmt.executeQuery(queryToTest)

      var rowCount = new Integer(1)
      val list = new java.util.ArrayList[util.List[String]]()
      while(rs.next()){
        val tuple = new util.ArrayList[String]()
        for (i <- 1 to rs.getMetaData.getColumnCount){
          tuple.add(rs.getString(1))
        }

        list.add(tuple)
      }

      val end2 = new Date()
      println(s"rows $rowCount")
      val jdbctime = end2.getTime() - start2.getTime()


      println(s"JDBI time: $jdbitime, JDBC time $jdbctime")
    }



    //run with jdbc
  }
}
