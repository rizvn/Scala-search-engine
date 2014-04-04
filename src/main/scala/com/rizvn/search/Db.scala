package com.rizvn.search

import org.skife.jdbi.v2.DBI
import org.apache.commons.dbcp.BasicDataSource
import org.skife.jdbi.v2.Handle

trait Db {
  protected var dbi:DBI = null
  protected var dbName:String

  def getDbi() : DBI = {
    if(dbi == null){
      val ds = new BasicDataSource()
      ds.setUrl(s"jdbc:sqlite:src/main/resources/$dbName")
      this.dbi = new DBI(ds)
    }
    return this.dbi
  }

  /**
   * Takes a function that takes a closer which takes Handle and return java.lang.Object
   * @param f  closure
   * @tparam Obj  Generic type
   * @return  return type
   */
  def withTransaction[Obj<: Object](f: (Handle) => Obj): Obj = {
    val h = getDbi().open()
    try{
      val result = f(h)
      return result
    }
    finally{
      h.close()
    }
  }
}