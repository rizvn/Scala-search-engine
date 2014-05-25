package com.rizvn.search

import org.skife.jdbi.v2.DBI
import org.apache.commons.dbcp.BasicDataSource
import org.skife.jdbi.v2.Handle

case class Database(driver:String, url:String, user:String=null, pass:String=null) {

  lazy val dbi : DBI = {
    val ds = new BasicDataSource()
    ds.setDriverClassName(driver)
    ds.setUrl(url)
    ds.setInitialSize(20)
    Option(user).map(ds.setUsername(_))
    Option(pass).map(ds.setPassword(_))
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