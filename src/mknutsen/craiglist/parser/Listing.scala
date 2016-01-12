package mknutsen.craiglist.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * Created by mknutsen on 1/11/16.
 */
class Listing(url: String, document: Document) {
  /**
   * Titles is in the postingtitle class
   */
  val title = document.select(".postingtitle").text()

  /**
   * the body has the ID postingboy
   */
  val postBody = document.select("#postingbody").text()

  /**
   * Images url is located somewhere in the iw oneimage class, ill parse that later
   */
  val imageLocation = document.select(".iw oneimage")

  /**
   * description is in attrgroup (stuff like condition and size is specified here)
   * It gets parsed into a hashtable of key being description type (condition) and value being description (new)
   * **this doesn't work yet **
   */
  val description = document.select(".attrgroup").text()
  val descriptionList = description.split("[ :]+")
  val descriptionTable = new mutable.HashMap[String, String]()
  for (i <- 0 to descriptionList.length - 2 by 2) {
    println(descriptionList(i) + " " + descriptionList(i + 1))
    descriptionTable.put(descriptionList(i), descriptionList(i + 1))
  }


  override def toString = "Listed: " + title + " at " + url + "  " + descriptionTable
}

object Listing {


  def getElements(url: String, selector: String): Elements = {
    Jsoup.connect(url).get().select(selector)
  }

  def getUrls(elems: Elements): List[String] = {
    (for (elem <- elems) yield elem.attr("href") mkString) toList
  }

  def elementToString(elems: List[Elements]): List[String] = {
    for (elem <- elems) yield elem.text mkString
  }
}
