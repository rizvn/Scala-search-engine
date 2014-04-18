package com.rizvn.search

import org.junit.Test
import org.junit.Assert._

/**
 * Created by riz on 18/04/2014.
 */
class SearcherTest {



  @Test
  def testBuildSqlQuery(){
    //searcher with mocked method
    val searcher = new Searcher()
    val query = searcher.buildSqlQuery("hello world this is a search")
    println(query)
    assertNotNull(query)
  }
}
