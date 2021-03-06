package mknutsen.craiglist.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

//import scala.collection.JavaConversions._
import scala.collection.JavaConversions._

/**
	* @param url
	* url of the listing
	* @param title
	* title of the listing
	* @param postBody
	* body of the posting
	* @param imageLocation
	* image information to be parsed later
	* @param descriptionTable
	* table of descriptions (condition->excellent, etc)
	* @param cost
	* how much this cost
	* @param datePosted
	* date it was posted
	* @param dateTakenDown
	* date it was taken down if known
	*/
class Listing ( url : String, title : String, postBody : String, imageLocation : Elements,
								descriptionTable : scala.collection.mutable.HashMap[ String, String ], cost : Int,
								datePosted : String, dateTakenDown : String ) {


	final override def toString = {
		( if ( getIsDead ( ) ) "dead link: " else "Listed: " ) + title + " for $" + cost + " at " + url + "  " +
			descriptionTable + "posted on " + datePosted + ( if ( datePosted.equals ( "" ) ) {
			" taken down on " +
				dateTakenDown
		} else {
			""
		} )

	}

	final def getIsDead ( ) = !"".equals ( dateTakenDown )

	/**
		* @param url
		* @param document
		*/
	def this ( url : String, document : Document ) = {
		this ( url, document.select ( ".postingtitle" ).text ( ), document.select ( "#postingbody" ).text ( ),
					 document.select
					 ( ".iw oneimage" ), Listing.parseDescription ( document.select ( ".attrgroup" ) ),
					 Listing.parsePriceFromString ( Listing
																						.extractStringAfterIndicator ( '0', '9', document.text ( ), "$" ) ),
					 Listing.extractStringBetweenIndicators ( "datetime=\"", "\"",
																										document.select ( "#display-date" ).toString ), "" )
	}

	final def getTitle ( ) = title

	final def getPrice ( ) = cost

	final def getURL ( ) = url

	final def getBody ( ) = postBody

	final def getDescription ( ) = descriptionTable

	final def getDateTakenDown ( ) = datePosted

	final def getDatePosted ( ) = dateTakenDown
}

final object Listing {
	def parseDescription ( descriptionList : Elements ) : scala.collection.mutable.HashMap[ String, String ] = {
		var descriptionText = if ( descriptionList == null ) "" else descriptionList.toString ( ).toLowerCase ( )
		val descriptionTable = new scala.collection.mutable.HashMap[ String, String ]( )
		while ( descriptionText.indexOf ( "<span>" ) > -1 && descriptionText.indexOf ( "</b>" ) > -1 ) {
			if ( descriptionText.indexOf ( "<span>" ) + "<span>".length != descriptionText.indexOf ( "<b>" ) ) {
				val key = Listing.extractStringBetweenIndicators ( "<span>", ":", descriptionText )
				val value = Listing.extractStringBetweenIndicators ( "<b>", "</b>", descriptionText )
				descriptionTable.put ( key, value )
				val newLoc = descriptionText.indexOf ( "</b>" ) + "</b>".length
				descriptionText = descriptionText.substring ( newLoc )
			} else {
				descriptionText = descriptionText.substring ( descriptionText.indexOf ( "</span>" ) + "</span>".length )
			}
		}
		return descriptionTable
	}

	def extractStringBetweenIndicators ( startString : String, endString : String, str : String ) : String = {
		val startLoc = str.indexOf ( startString )
		if ( startLoc < 0 ) {
			return ""
		}
		val strStartingPoint = str.substring ( startLoc + startString.length )
		strStartingPoint.substring ( 0, strStartingPoint.indexOf ( endString ) )
	}

	def extractStringAfterIndicator ( low : Character, high : Character, str : String, indicator : String ) : String = {
		var returnVal = ""
		var start = str.indexOf ( indicator ) + indicator.length
		val strArray = str.toCharArray
		while ( start < strArray.length && strArray ( start ) <= high && strArray ( start ) >= low ) {
			returnVal += strArray ( start )
			start += 1
		}
		return returnVal
	}

	def getElements ( url : String, selector : String ) : Elements = {
		Jsoup.connect ( url ).get ( ).select ( selector )
	}

	def getUrls ( elems : Elements ) : List[ String ] = {
		( for ( elem <- elems ) yield elem.attr ( "href" ) mkString ) toList
	}

	def elementToString ( elems : List[ Elements ] ) : List[ String ] = {
		for ( elem <- elems ) yield elem.text mkString
	}

	def tokenizeText ( string : String ) = {

	}


	/**
		* @param amount
		* string representing the amount
		* @return cost of the item is $-1 if the price wasn't actually listed
		*/
	def parsePriceFromString ( amount : String ) : Int = {
		val itemCost = if ( amount.length ( ) > 0 ) Integer.parseInt ( amount ) else -1
		return itemCost
	}
}
