package com.rizvn.search

import java.io.File
import java.lang.Long
import org.jsoup.Jsoup
import org.junit.Assert._
import org.junit.Test
import org.skife.jdbi.v2.Handle
import org.skife.jdbi.v2.tweak.HandleCallback


class CrawlerTest {
  
  val crawler = new Crawler("searchindex.db")
  
  @Test
  def dbiInitTest():Unit = {
    val dbi = this.crawler.getDbi
    assertNotNull(dbi)

    val count:Long = crawler.runQuery()
    assertTrue(count > 0 )
  }

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
  
  /*
  @Test
  def testPage(){
    val doc = Jsoup.parse(new File("corpus/articles/0/_/(/0_(number).html"), "UTF-8")
    val text = doc.text()
    assertNotNull(text)
  }
  * 
  * 
  */

  @Test
  def testSeparateWords(){
    val result = crawler.seperateWords("hello world")
    assertTrue(result.length == 2)
  }
}