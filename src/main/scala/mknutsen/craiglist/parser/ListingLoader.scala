package mknutsen.craiglist.parser

import org.jsoup.Jsoup

import scala.io.Source

/**
  * Created by mknutsen on 1/15/16.
  */
object ListingLoader {
  def processLineSegment(lineSegments: Array[String]): Listing = {
    return new Listing(lineSegments(2), Jsoup.connect(lineSegments(2)).get())
  }

  def serializeListing(listing: Listing): String = {
    listing.getTitle() + ";" + listing.getURL()
  }

  def main(args: Array[String]): Unit = {
    var listings = List[Listing]()
    for (line <- Source.fromFile(args(0)).getLines()) {
      val lineSegments = line.split(";")
      val newListing = ListingLoader.processLineSegment(lineSegments)
      println(newListing)
      listings = newListing :: listings
    }
    print(listings)
  }
}
