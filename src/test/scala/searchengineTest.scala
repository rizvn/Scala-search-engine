import java.lang.Long
import org.junit.Assert._
import org.junit.Test
import org.skife.jdbi.v2.Handle
import org.skife.jdbi.v2.tweak.HandleCallback

/**
 * Created by riz on 26/03/2014.
 */
class SearchengineTest {

  @Test
  def dbiInitTest():Unit = {
    val crawler = new Crawler("searchindex.db")
    val dbi = crawler.getDbi
    assertNotNull(dbi)

    val count:Long = crawler.runQuery()
    assertTrue(count > 0 )
  }

  @Test
  def getEntryIdTest() = {
     val crawler: Crawler = new Crawler("searchindex.db")
     val result = crawler.getEntryId("wordlist", "word", "rizvan")
     assertTrue(result> 0)

    crawler.getDbi.withHandle(new HandleCallback[Unit] {
      def withHandle(h: Handle): Unit = {
        h.execute("DELETE FROM WORDLIST WHERE WORD='rizvan'")
      }
    })
  }

}
