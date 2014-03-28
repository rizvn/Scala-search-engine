import java.lang.Long
import org.apache.commons.dbcp.BasicDataSource
import org.skife.jdbi.v2.util.LongMapper
import org.skife.jdbi.v2.{Handle, DBI}

trait Db {
  protected var dbi:DBI = null
  protected var dbName:String

  def getDbi() : DBI = {
    if(dbi == null){
      val ds = new BasicDataSource()
      ds.setUrl(s"jdbc:sqlite:$dbName")
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
            result = handle.createStatement(s"INSERT into $table($field) values ('$value')").executeAndReturnGeneratedKeys(LongMapper.FIRST).first()
          }
          return result
      }
  }

}

class Searcher(dbName:String){

}