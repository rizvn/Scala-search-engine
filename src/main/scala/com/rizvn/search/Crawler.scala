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
 * @param dbName  will be automatically be assigned to dbName var in Db Trait
 */
class Crawler(var dbName:String) extends Db{

  def getEntryId(table:String, field:String, value:String, createNew:Boolean = true) : Long= {
      withHandle(handle =>{
          var result:Long = handle.createQuery(s"SELECT rowid from $table where $field='$value'")
                                   .map(LongMapper.FIRST)
                                   .first()
          if(createNew && result == null){
            result = handle.createStatement(s"INSERT into $table($field) values ('$value')")
                           .executeAndReturnGeneratedKeys(LongMapper.FIRST).first()
          }
          return result
      })
  }

  /**
   * Separate words and remove ones in ignored words set
   * @param text some text
   * @return array of words excluding ones from ignoreword
   */
  def seperateWords(text:String, ignoreWords:Set[String]= Set("the","of", " ", "to", "and", "a", "in", "is", "it")) : Array[String] = {
    text.split("\\W").filter( !ignoreWords.contains(_) )
  }

  def isIndexed(url:String): Boolean = {
    withHandle(handle => {
      //check url exists
      val result:Long = handle.createQuery(s"SELECT rowid from urllist where url = '$url'")
                         .map(LongMapper.FIRST)
                         .first()
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

  def addToIndex(url:String, page:Document):Unit = {
    if(isIndexed(url)) return;

    println(s"Indexing url: $url")
    val pageText = page.text()
    val words = seperateWords(pageText)

    val urlId = getEntryId("urllist", "url", url)

    for((word, index) <- words.zipWithIndex ){ //zip with index will get array and create tuples ("word1", 0), ("word2", 1) then we loop through each tuple in turn
      withHandle(handle =>{
        val wordId = getEntryId("wordList", "word", word)
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
      addToIndex(url, Jsoup.connect(url).get())
    })
  }
  
  def createSchema() = {
    withHandle(h =>{
      h.execute("create table urllist(url)")
      h.execute("create table wordlist(word)")
      h.execute("create table wordlocation(urlid,wordid,location)")
      h.execute("create table link(fromid integer,toid integer)")
      h.execute("create table linkwords(wordid,linkid)")
      h.execute("create index wordidx on wordlist(word)")
      h.execute("create index urlidx on urllist(url)")
      h.execute("create index wordurlidx on wordlocation(wordid)")
      h.execute("create index urltoidx on link(toid)")
      h.execute("create index urlfromidx on link(fromid)")
      None
    })
  }

}