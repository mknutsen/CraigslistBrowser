package mknutsen.craiglist.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

//import scala.collection.JavaConversions._

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * Created by mknutsen on 1/11/16.
 */
class Listing(url: String, document: Document) {
  /**
   * Titles is in the postingtitle class
   */
  private val title = document.select(".postingtitle").text()

  /**
   * the body has the ID postingboy
   */
  private val postBody = document.select("#postingbody").text()

  /**
   * Images url is located somewhere in the iw oneimage class, ill parse that later
   */
  private val imageLocation = document.select(".iw oneimage")

  /**
   * description is in attrgroup (stuff like condition and size is specified here)
   * It gets parsed into a hashtable of key being description type (condition) and value being description (new)
   * **this doesn't work yet **
   */
  private val description = document.select(".attrgroup").text()
  private val descriptionList = description.split("[ :]+")
  private val descriptionTable = new mutable.HashMap[String, String]()
  for (i <- 0 to descriptionList.length - 2 by 2) {
    //    println(descriptionList(i) + " " + descriptionList(i + 1))
    descriptionTable.put(descriptionList(i), descriptionList(i + 1))
  }
  private val textDocument = document.text()

  /**
   * Parses out the cost of the object
   */
  private var documentStringCounter = textDocument.indexOf('$') + 1
  private var amount = 0
  var currentChar = textDocument.charAt(documentStringCounter)
  while (documentStringCounter < textDocument.length && currentChar >= 48 && currentChar <= 57) {
    amount *= 10
    amount += Integer.parseInt(textDocument.charAt(documentStringCounter) + "")
    documentStringCounter += 1
    currentChar = textDocument.charAt(documentStringCounter)
  }

  private val itemCost = amount


  override def toString = "Listed: " + title + " for $" + itemCost + " at " + url + "  " + descriptionTable
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
