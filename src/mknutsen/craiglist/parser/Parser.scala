package mknutsen.craiglist.parser

import org.jsoup.Jsoup

import scala.collection.JavaConversions._

/**
 * Created by mknutsen on 1/10/16.
 */
object Parser {
  var baseURL = ".craigslist.org"
  val searchStart = "/search/sss"

  def main(args: Array[String]): Unit = {
    baseURL = "http://" + args(0) + baseURL
    val listings = getListings(baseURL + searchStart)
    println(listings(0))
    for (item <- listings(0).description.split(":")) println(item)
  }

  def getListings(url: String): List[Listing] = {
    val elems = Listing.getElements(url, ".i")
    while (elems.length > 1) {
      elems.remove(1)
    }
    val links = Listing.getUrls(elems)
    for (link <- links) yield new Listing(baseURL + link, Jsoup.connect(baseURL + link).get())
  }
}
