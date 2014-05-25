package com.rizvn.search

import org.skife.jdbi.v2.util.LongMapper
import scala.collection.mutable
import java.sql.ResultSet
import org.skife.jdbi.v2.tweak.ResultSetMapper
import org.skife.jdbi.v2.StatementContext

/**
 * @author Riz
 */
class Searcher {
  val database = Database(driver = "com.mysql.jdbc.Driver", url = "jdbc:mysql://localhost:8889/search_engine", user = "root", pass="root")


  def getWordId(word:String) : Long = {
    database.withHandle(h=>
      h.createQuery(s"SELECT id from WORDLIST where WORD = '$word'").map(LongMapper.FIRST).first()
    )
  }

  def buildSqlQuery(search: String) : java.util.List[java.util.List[Long]] = {
    val selectClause = new mutable.StringBuilder()
    val fromClause = new mutable.StringBuilder()
    val whereClause = new mutable.StringBuilder()

    selectClause.append("w0.urlid") //add the url id to select
    var i = 0
    val words = search.split(" ")

    //get all word ids
    words.foreach(word => {
      val wordId: Option[Long] = Option(getWordId(word))

      wordId.map((wordId) => {
        //if word id found then do the closure otherwise dont do anything, this replace wordId != null
        selectClause.append(s", w$i.location") //add word to select

        if (i > 0) fromClause.append(",")
        fromClause.append(s" WORDLOCATION w$i") //join word location table for each word

        if (whereClause.size > 0) whereClause.append(" AND ")
        whereClause.append(s" w$i.wordId=$wordId ")

        if (i > 0) {
          whereClause.append(s" AND  w0.urlid = w$i.urlid ") //make sure all location rows have the same url id
        }

        i = i + 1 //TODO: dirty need to fix
      })
    })

    val query = "SELECT " + selectClause.toString() +
      " FROM " + fromClause.toString() +
      " WHERE " + whereClause.toString()

    val result = database.withHandle(h => {
      h.createQuery(query)
        .map(new SearchQueryMapper())
        .list()
    })

    result
  }
}

class SearchQueryMapper extends ResultSetMapper[java.util.List[Long]]{
  override def map(index: Int, rs: ResultSet, ctx: StatementContext): java.util.List[Long] = {
    val row = new java.util.ArrayList[Long]()
    for(i <- 1 to rs.getMetaData.getColumnCount){
      row.add(rs.getLong(i))
    }
    row
  }
}