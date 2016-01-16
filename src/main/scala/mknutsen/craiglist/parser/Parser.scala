package mknutsen.craiglist.parser

import java.io.{BufferedWriter, FileWriter, PrintWriter}

import org.jsoup.Jsoup
//http://washingtondc.craigslist.org/search/sss?s=100&is_paid=all&query=car&search_distance_type=mi&sort=rel
/**
  * Created by mknutsen on 1/10/16.
  */
object Parser {
  var baseURL = ".craigslist.org"
  val searchStart = "/search/sss?s"

  /**
    *
    * @param args
    * [city in a string like washingtondc for washingtondc.craigslist.com, number of pages to look through, file to
    * write to]
    */
  def main(args: Array[String]): Unit = {
    val searchPostfix = if(args.length > 3) args(3) else ""
    baseURL = "http://" + args(0) + baseURL
    val numPages = if (args.length >= 2) Integer.parseInt(args(1)) else 1
    var listings: List[Listing] = List()
    for (i <- 0 to numPages - 1) {
      val tempListings = getListings(baseURL + searchStart + i * 100 + searchPostfix)
      listings = tempListings ::: listings
    }
    println("number of listings: " + listings.length)
    val out: PrintWriter = if (args.length > 2) new PrintWriter(new BufferedWriter(new FileWriter(args(2),
      false)))
    else null
    for (listing <- listings) {
      println(listing)
      if (out != null) out.println(ListingLoader.serializeListing(listing))
    }
    if (out != null) out.close()
  }

  def getListings(url: String): List[Listing] = {
    val elems = Listing.getElements(url, ".i")
    while (elems.size() > 15) {
      elems.remove(0)
    }
    val links = Listing.getUrls(elems)
    for (link <- links) yield new Listing(baseURL + link, Jsoup.connect(baseURL + link).get())

  }
}
