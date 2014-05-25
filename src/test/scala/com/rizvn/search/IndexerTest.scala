package com.rizvn.search

import java.io.File
import java.lang.Long
import org.jsoup.Jsoup
import org.junit.Assert._
import org.junit.Test
import org.skife.jdbi.v2.{DBI, Handle}
import org.skife.jdbi.v2.tweak.HandleCallback
import scala.collection.mutable
import org.jsoup.nodes.Element
import java.util.Date
import org.apache.commons.dbcp.BasicDataSource

/**
 * @author Riz
 */
class IndexerTest {
  
  val indexer = new Indexer()
  
  @Test
  def getEntryIdTest() = {
     val result = this.indexer.getOrCreateEntryId("wordlist", "word", "rizvan")
     assertTrue(result> 0)

    indexer.database.dbi.withHandle(new HandleCallback[Unit] {
      def withHandle(h: Handle): Unit = {
        h.execute("DELETE FROM WORDLIST WHERE WORD='rizvan'")
      }
    })
  }

  @Test
  def testGetFilesInDir(){
    val files = indexer.getFilesInDir("wiki")
    assertTrue(files.size == 5641)
  }

  @Test
  def testSeparateWords(){
    val result = indexer.seperateWords("hello world in") //'in' will be ignored
    assertTrue(result.length == 2)
  }

  @Test
  def testCreateSchema(){
    indexer.createSchema()
  }

  @Test
  def testIndex(){
    //indexer.createSchema()
    val files = indexer.getFilesInDir("wiki")
    indexer.startIndexing(files)
  }



}