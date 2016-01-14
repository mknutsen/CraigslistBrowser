package mknutsen.craiglist.parser

import org.jsoup.Jsoup

/**
  * Created by mknutsen on 1/10/16.
  */
object Parser {
  var baseURL = ".craigslist.org"
  val searchStart = "/search/sss?s"

  /**
    *
    * @param args
   * [city in a string like washingtondc for washingtondc.craigslist.com, number of pages to look through]
    */
  def main(args: Array[String]): Unit = {
    baseURL = "http://" + args(0) + baseURL
    val numPages = if (args.length >= 2) Integer.parseInt(args(1)) else 1
    var listings: List[Listing] = List()
    for (i <- 0 to numPages - 1) {
      val tempListings = getListings(baseURL + searchStart + i * 100)
      listings = tempListings ::: listings
    }
    println("number of listings: " + listings.length)
    for (listing <- listings) println(listing)
  }

  def getListings(url: String): List[Listing] = {
    val elems = Listing.getElements(url, ".i")
    while (elems.size() > 15) {
      elems.remove(1)
    }
    val links = Listing.getUrls(elems)
    for (link <- links) yield new Listing(baseURL + link, Jsoup.connect(baseURL + link).get())
  }
}
