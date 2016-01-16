package mknutsen.craiglist.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

//import scala.collection.JavaConversions._
import scala.collection.JavaConversions._

/**
  * Created by mknutsen on 1/11/16.
  */
class Listing(url: String, document: Document) {
  private val isDead = document.text().indexOf("Page Not Found") != -1
  /**
    * Titles is in the postingtitle class
    */
  private val title = if (isDead) "" else document.select(".postingtitle").text()

  /**
    * the body has the ID postingbody
    */
  private val postBody = if (isDead) "" else document.select("#postingbody").text()

  /**
    * Images url is located somewhere in the iw oneimage class, ill parse that later
    */
  private val imageLocation = if (isDead) "" else document.select(".iw oneimage")

  /**
    * description is in attrgroup (stuff like condition and size is specified here)
    * It gets parsed into a hashtable of key being description type (condition) and value being description (new)
    * **this doesn't work yet **
    */
  private val description = if (isDead) new Elements() else document.select(".attrgroup")
  private val descriptionTable = parseDescription(description)
  private val textDocument = if (isDead) "" else document.text()

  /**
    * Parses out the cost of the object
    */
  private val amount = Listing.extractStringAfterIndicator('0', '9', textDocument, "$")

  /**
    * cost of the item is $-1 if the price wasn't actually listed
    */
  private val itemCost = if (amount.length() > 0) Integer.parseInt(amount) else -1

  if (itemCost == -1) {
    System.err.println(url)
  }

  private def parseDescription(descriptionList: Elements): scala.collection.mutable.HashMap[String, String] = {
    var descriptionText = if (isDead) "" else descriptionList.toString().toLowerCase()
    val descriptionTable = new scala.collection.mutable.HashMap[String, String]()
    while (descriptionText.indexOf("<span>") > -1 && descriptionText.indexOf("</b>") > -1) {
      val key = Listing.extractStringBetweenIndicators("<span>", ":", descriptionText)
      val value = Listing.extractStringBetweenIndicators("<b>", "</b>", descriptionText)
      descriptionTable.put(key, value)
      val newLoc = descriptionText.indexOf("</b>") + "</b>".length
      descriptionText = descriptionText.substring(newLoc)
    }
    return descriptionTable
  }


  def getTitle() = title

  def getPrice() = itemCost

  def getURL() = url

  def getIsDead() = isDead

  override def toString = {
    if (isDead) {
      "Link is dead at " + url
    } else {
      "Listed: " + title + " for $" + itemCost + " at " + url + "  " + descriptionTable
    }
  }
}

object Listing {
  def extractStringAfterIndicator(low: Character, high: Character, str: String, indicator: String): String = {
    var returnVal = ""
    var start = str.indexOf(indicator) + indicator.length
    val strArray = str.toCharArray
    while (start < strArray.length && strArray(start) <= high && strArray(start) >= low) {
      returnVal += strArray(start)
      start += 1
    }
    return returnVal
  }

  def extractStringBetweenIndicators(startString: String, endString: String, str: String): String = {
    val startLoc = str.indexOf(startString)
    if (startLoc < 0) {
      return ""
    }
    val strStartingPoint = str.substring(startLoc + startString.length)
    return strStartingPoint.substring(0, strStartingPoint.indexOf(endString))
  }

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
