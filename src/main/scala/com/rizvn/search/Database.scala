package com.rizvn.search

import org.skife.jdbi.v2.DBI
import org.apache.commons.dbcp.BasicDataSource
import org.skife.jdbi.v2.Handle

case class Database(driver:String, url:String) {

  lazy val dbi : DBI = {
    val ds = new BasicDataSource()
    ds.setDriverClassName(driver)
    ds.setUrl(url)
    new DBI(ds)
  }

  def withHandle[T](f: (Handle) => T): T = {
    val h = dbi.open()
    try{
      val result = f(h)
      result
    }finally{
      h.close()
    }
  }

}