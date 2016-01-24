package mknutsen.craiglist.parser

import java.io.{BufferedWriter, FileWriter, PrintWriter}
import java.time.{LocalDateTime, ZoneId}

import org.jsoup.Jsoup

import scala.collection.mutable
import scala.io.Source

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
		* write locationIdentifier numListings fileOut.txt &is_paid=all&query=car&search_distance_type=mi&sort=rel
		* or
		* load open.txt closed.txt
		*/
	def main ( args : Array[ String ] ) : Unit = {
		args ( 0 ) match {
			case "write" ⇒ write ( args )
			case "load" ⇒ load ( args )
			case _ ⇒ throw new IllegalArgumentException ( " write locationIdentifier numListings fileOut.txt " +
																											"&is_paid=all&query=car&search_distance_type=mi&sort=rel\n\t\t" +
																											" " +
																											"or\\n\\t\\t* load file.txt" )
		}
	}

	def load ( args : Array[ String ] ) : Unit = {
		val closedFile = new PrintWriter ( new BufferedWriter ( new FileWriter ( args ( 2 ), true ) ) )
		var listings = List [ Listing ]( )
		for ( line <- Source.fromFile ( args ( 1 ) ).getLines ( ) ) {
			val lineSegments = line.split ( ";" )
			val newListing = processLineSegment ( lineSegments )
			if ( newListing.getIsDead ( ) ) {
				closedFile.println ( serializeListing ( newListing ) )
			} else {
				listings = newListing :: listings
			}
		}
		closedFile.close ( )
		val openFile = new PrintWriter ( new BufferedWriter ( new FileWriter ( args ( 1 ),
																																					 false ) ) )
		printListings ( listings, openFile )
		println ( "number of listings: " + listings.size )
		openFile.close ( )
	}

	def write ( args : Array[ String ] ) : Unit = {
		val searchPostfix = if ( args.length > 4 ) args ( 4 ) else ""
		baseURL = "http://" + args ( 1 ) + baseURL
		val numListings = if ( args.length >= 3 ) Integer.parseInt ( args ( 2 ) ) else 1
		var listings : List[ Listing ] = List ( )
		var i = 0
		while ( listings.size < numListings ) {
			val tempListings = getListings ( baseURL + searchStart + i * 100 + searchPostfix,
																			 numListings - listings.size )
			i += 1
			listings = tempListings ::: listings
		}
		println ( "number of listings: " + listings.length )
		val out : PrintWriter = if ( args.length > 3 ) {
			new PrintWriter ( new BufferedWriter ( new FileWriter ( args ( 3 ),
																															true ) ) )
		} else {
			null
		}
		if ( out == null ) {
			printListings ( listings )
		} else {
			printListings ( listings = listings, printWriter = out )
			out.close ( )
		}
	}

	def getListings ( url : String, num : Int ) : List[ Listing ] = {
		val elems = Listing.getElements ( url, ".i" )
		while ( elems.size ( ) > num ) {
			elems.remove ( 0 )
		}
		val links = Listing.getUrls ( elems )
		for ( link <- links ) yield new Listing ( baseURL + link, Jsoup.connect ( baseURL + link ).get ( ) )
	}

	/**
		* havent tested this, not thinking its gonna work but we will see.
		*
		* @param s
		* @return
		*/
	def parseDescription ( s : String ) : mutable.HashMap[ String, String ] = {
		val descriptionTable = new scala.collection.mutable.HashMap[ String, String ]( )
		val descriptions = s.substring ( 4 ).split ( "," )
		println ( descriptions )
		for ( description <- descriptions ) {
			val keyEnd = description.indexOf ( " -> " )
			val valBegin = keyEnd + " -> ".length
			descriptionTable.put ( description.substring ( 0, keyEnd ), description.substring ( valBegin ) )
		}
		return descriptionTable
	}

	def processLineSegment ( lineSegments : Array[ String ] ) : Listing = {
		val document = Jsoup.connect ( lineSegments ( 0 ) ).get ( )
		if ( document.toString.indexOf ( "No web page for this address" ) == -1 ) {
			return new Listing ( lineSegments ( 0 ), document )
		} else {
			val newYork = ZoneId.of ( "America/New_York" )
			return new Listing ( lineSegments ( 0 ), lineSegments ( 1 ), lineSegments ( 3 ), null,
													 parseDescription ( lineSegments ( 4 ) ), Listing.parsePriceFromString ( lineSegments ( 2
																																																								) ),
													 lineSegments ( 5 ),
													 if ( lineSegments.length > 6 && lineSegments ( 6 ) != "" ) {
														 lineSegments ( 6 )
													 }
													 else {
														 LocalDateTime.now ( newYork ).toString ( )
													 } )
			// to
			// implement later
		}
	}

	def serializeListing ( listing : Listing ) : String = {
		listing.getURL ( ) + ";" + listing.getTitle ( ) + ";" + listing.getPrice ( ) + ";" + listing.getBody ( ) + ";" +
			listing.getDescription ( ).toString ( ) + listing.getDatePosted ( ) + ";" + listing.getDateTakenDown ( )
	}

	def printListings ( listings : List[ Listing ] ) = {
		for ( listing ← listings ) println ( listing )
	}

	def printListings ( listings : List[ Listing ], printWriter : PrintWriter ) = {
		for ( listing ← listings ) printWriter.println ( serializeListing ( listing ) )
	}
}
