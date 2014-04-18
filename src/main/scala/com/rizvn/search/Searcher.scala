package com.rizvn.search

import org.skife.jdbi.v2.util.LongMapper
import scala.collection.mutable

/**
 * Created by riz on 18/04/2014.
 */
class Searcher {
  val database = Database(driver = "org.hsqldb.jdbc.JDBCDriver", url = "jdbc:hsqldb:file:db/searchIndex")

  def getWordId(word:String) : Long = {
    database.withHandle(h=>
      h.createQuery(s"SELECT id from WORDLIST where WORD = '$word'").map(LongMapper.FIRST).first()
    )
  }

  def buildSqlQuery(search: String) = {
    val selectClause  = new mutable.StringBuilder()
    val fromClause  = new mutable.StringBuilder()
    val whereClause = new mutable.StringBuilder()

    selectClause.append("w0.urlid") //add the url id to select
    var i = 0
    val words = search.split(" ")

    //get all word ids
    words.foreach(word => {
      val wordId: Option[Long] = Option(1L)       //Option(getWordId(word))

      wordId.map((wordId) => {                    //if word id found then do the closure otherwise dont do anything, this replace wordId != null
        selectClause.append(s", w$i.location")    //add word to select

        if(i>0) fromClause.append(",")
        fromClause.append(s" WORDLOCATION w$i")  //join word location table for each word

        if(whereClause.size > 0) whereClause.append(" AND ")
        whereClause.append(s" w$i.wordId=$wordId ")

        if(i > 0){
          val a = i-1
          val b = i
          whereClause.append(s" AND  w$a.urlid = w$b.urlid ")
        }

        i = i + 1                                 //TODO: dirty need to fix
      })
    })
    "SELECT " + selectClause.toString() +
    " FROM "+fromClause.toString() +
    " WHERE "+whereClause.toString()

  }
}
