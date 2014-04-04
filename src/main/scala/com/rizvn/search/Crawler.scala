package com.rizvn.search

import java.lang.Long
import org.apache.commons.dbcp.BasicDataSource
import org.skife.jdbi.v2.util.LongMapper
import org.skife.jdbi.v2.{Handle, DBI}

/**
 * @param dbName  will be automatically be assigned to dbName var in Db Trait
 */
class Crawler(var dbName:String) extends Db{

  def runQuery() : Long = {
     withTransaction{ handle =>
       handle.createQuery("SELECT Count(*) FROM URLLIST" )
             .map(LongMapper.FIRST)
             .first()
     }
  }

  def getEntryId(table:String, field:String, value:String, createNew:Boolean = true) : Long= {
      withTransaction{ handle =>
          var result:Long = handle.createQuery(s"SELECT rowid from $table where $field='$value'")
                                   .map(LongMapper.FIRST)
                                   .first()
          if(createNew && result == null){
            result = handle.createStatement(s"INSERT into $table($field) values ('$value')")
                           .executeAndReturnGeneratedKeys(LongMapper.FIRST).first()
          }
          return result
      }
  }

  def seperateWords(text:String) : Array[String] = {
    text.split("\\W")
  }

}