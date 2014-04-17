package com.rizvn.search

import java.lang.Long
import org.apache.commons.dbcp.BasicDataSource
import org.skife.jdbi.v2.util.LongMapper
import org.skife.jdbi.v2.{Handle, DBI}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.mutable
import scala.collection.JavaConversions._

/**
 * @param dbUrl  will be automatically be assigned to dbName var in Db Trait
 */
class Indexer{

  val database = Database(driver = "org.hsqldb.jdbc.JDBCDriver", url = "jdbc:hsqldb:mem:mymemdb")

  def getOrCreateEntryId(table:String, field:String, value:String) : Long = {
      database.withHandle(h =>{
          val result = Option(h.createQuery(s"SELECT rowid from $table where $field='$value'")
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
      val result:Long = handle.createQuery(s"SELECT rowid from urllist where url = '$url'")
                         .map(LongMapper.FIRST).first
      if(result != null){
        //url exists then check if any words are mapped to this url
        val wordCount = handle.createQuery(s"SELECT COUNT(*) FROM wordlocation where urlid=$result")
                           .map(LongMapper.FIRST)
                           .first()
        return wordCount != null
      }
      return false
    })
  }

  def getTextOnly(doc: Document) : String = {
    val sb = new mutable.StringBuilder()
    for(element <- doc.getAllElements()){
      val text = element.text();
      if(text.size > 1 ){
        sb.append(text.toLowerCase.trim)
      }
    }
    sb.toString()
  }

  def addToIndex(url:String, page:Document):Unit = {
    if(isIndexed(url)) return;

    println(s"Indexing url: $url")
    val pageText = page.text()
    val words = seperateWords(pageText)

    val urlId = getOrCreateEntryId("urllist", "url", url)

    var i = 0;
    for((word, index) <- words.view.zipWithIndex ){ //zip with index will get array and create tuples ("word1", 0), ("word2", 1) then we loop through each tuple in turn
      database.withHandle(handle =>{
        val wordId = getOrCreateEntryId("wordList", "word", word)
        //println(s"Word $word $wordId $index")
        handle.execute(s"INSERT INTO wordLocation(urlid, wordid, location) values('$urlId', '$wordId', '$index')")
        None //because with handle expects a return value
      })
    }
  }

  def getPageLinks(page:String) : Seq[String] = {
    val doc = Jsoup.connect(page).get()
    val links = doc.select("a")

    val urls = new mutable.MutableList[String]()

    for (a <- links){
      urls += a.attr("abs:href")
    }
    urls.toIndexedSeq
  }

  def crawl(pages:Seq[String]) = {
    pages.foreach(url => {
      //TODO: add page exists check here

      addToIndex(url, Jsoup.connect(url).get())
    })
  }
  
  def createSchema() = {
    database.withHandle(h =>{
      h.execute("create table URLLIST(ID IDENTITY, URL VARCHAR)")
      h.execute("create table WORDLIST(ID IDENTITY, WORD VARCHAR)")
      h.execute("create table WORD_LOCATION(ID IDENTITY, WORDID NUMERIC, LOCATION NUMERIC)")
      h.execute("create table URLWORDS(URLID NUMERIC, WORDID NUMERIC)")
      None
    })
  }

}