package mknutsen.craiglist.parser

import org.jsoup.Jsoup
import org.jsoup.select.Elements

import scala.collection.JavaConversions._


/**
 * Created by mknutsen on 1/10/16.
 */
object Parser {
  val baseURL = "http://washingtondc.craigslist.org"
  val searchStart = "/search/sss"

  def main(args: Array[String]): Unit = {
    val elems = getElements(baseURL + searchStart, ".i")
    val links = getUrls(elems)
    val docs = for (href <- links) yield getElements(baseURL + href, "#postingbody")
    print(docs)
  }

  def getElements(url: String, selector: String): Elements = {
    Jsoup.connect(url).get().select(selector)
  }

  def getUrls(elems: Elements): List[String] = {
    (for (elem <- elems) yield elem.attr("href") mkString) toList
  }
}
