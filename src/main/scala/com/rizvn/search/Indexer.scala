package com.rizvn.search

import java.lang.Long
import org.apache.commons.dbcp.BasicDataSource
import org.skife.jdbi.v2.util.LongMapper
import org.skife.jdbi.v2.{Handle, DBI}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.mutable
import scala.collection.JavaConversions._
import java.io.File

/**
 * @param dbUrl  will be automatically be assigned to dbName var in Db Trait
 */
class Indexer{

  val database = Database(driver = "org.hsqldb.jdbc.JDBCDriver", url = "jdbc:hsqldb:file:db/searchIndex")

  def getOrCreateEntryId(table:String, field:String, value:String) : Long = {
    database.withHandle(h =>{
      val result = Option(h.createQuery(s"SELECT id from $table where $field='$value'")
                           .map(LongMapper.FIRST).first)
      result match{
        case Some(value) => value
        case None => h.createStatement(s"INSERT into $table($field) values ('$value')")
                      .executeAndReturnGeneratedKeys(LongMapper.FIRST).first()
      }
    })
  }

  def seperateWords(text:String, ignoreWords:Set[String]= Set("the","of", " ", "to", "and", "a", "in", "is", "it")) : Array[String] = {
    text.split("\\W").filter( !ignoreWords.contains(_) )
  }

  def isIndexed(url:String): Boolean = {
    database.withHandle(handle => {
      //check url exists
      val result:Long = handle.createQuery(s"SELECT COUNT(*) from urllist where url = '$url'")
                         .map(LongMapper.FIRST).first
      return result != 0
    })
  }

  def addToIndex(url:String, page:Document):Unit = {
    if(isIndexed(url)){ println(s"Already indexed $url"); return; }

    println(s"Indexing: $url")
    val pageText = page.text()
    val words = seperateWords(pageText)

    val urlId = getOrCreateEntryId("urllist", "url", url)

    for((word, index) <- words.view.zipWithIndex){ //zip with index will get array and create tuples ("word1", 0), ("word2", 1) then we loop through each tuple in turn
      database.withHandle(handle =>{
        val wordId = getOrCreateEntryId("wordList", "word", word)
        //println(s"Word $word $wordId $index")
        handle.execute(s"INSERT INTO WORDLOCATION(urlid, wordid, location) values('$urlId', '$wordId', '$index')")
      })
    }
  }

  def getFilesInDir(dir:String) : Seq[String]= {
    val folder = new File(dir)
    val fileList = mutable.MutableList[String]()
    folder.listFiles().foreach(fileList += _.getAbsolutePath)
    fileList.toSeq
  }

  def startIndexing(pages:Seq[String]) = {
    pages.foreach(url => {
      addToIndex(url, Jsoup.parse(new File(url), "UTF-8"))
    })
  }
  
  def createSchema() = {
    database.withHandle(h =>{
      h.execute("create table URLLIST(ID IDENTITY, URL VARCHAR(255))")
      h.execute("create table WORDLIST(ID IDENTITY, WORD VARCHAR(255))")
      h.execute("create table WORDLOCATION(URLID NUMERIC, WORDID NUMERIC, LOCATION NUMERIC)")
      h.commit()
    })
  }

}