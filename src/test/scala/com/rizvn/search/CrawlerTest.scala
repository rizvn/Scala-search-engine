package com.rizvn.search

import java.io.File
import java.lang.Long
import org.jsoup.Jsoup
import org.junit.Assert._
import org.junit.Test
import org.skife.jdbi.v2.Handle
import org.skife.jdbi.v2.tweak.HandleCallback
import scala.collection.mutable
import org.jsoup.nodes.Element


class CrawlerTest {
  
  val crawler = new Crawler("searchindex.db")
  
  @Test
  def getEntryIdTest() = {
     val result = this.crawler.getEntryId("wordlist", "word", "rizvan")
     assertTrue(result> 0)

    crawler.getDbi.withHandle(new HandleCallback[Unit] {
      def withHandle(h: Handle): Unit = {
        h.execute("DELETE FROM WORDLIST WHERE WORD='rizvan'")
      }
    })
  }
  
  @Test
  def testGetPageLinks(){
    val result = crawler.getPageLinks("http://localhost:8888/wiki")
    assertTrue(result.size == 5642)
  }

  @Test
  def testSeparateWords(){
    val result = crawler.seperateWords("hello world in") //'in' will be ignored
    assertTrue(result.length == 2)
  }

  @Test
  def testCreateSchema(){
    val newCrawler = new Crawler("newIndex.db");
    newCrawler.createSchema()
    val pages = crawler.getPageLinks("http://localhost:8888/wiki")
    crawler.crawl(pages)
  }
}