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
	val searchStart = "/search/sss?s"
	var baseURL = ".craigslist.org"

	/**
		*
		* @param args
		* write locationIdentifier numListings open.txt closed.txt &is_paid=all&query=car&search_distance_type=mi&sort=rel
		* or
		* load open.txt closed.txt
		*/
	def main ( args : Array[ String ] ) : Unit = {
		val configFile : mutable.HashMap[ String, String ] = loadConfigFile ( args ( 0 ) )
		val actionType = configFile.get ( "action" )
		if ( actionType == null || actionType.isEmpty ) {

		} else if ( "write".equals ( actionType.get ) ) {
			write ( configFile )
		} else if ( "load".equals ( actionType.get ) ) {
			load ( configFile )
		} else {

		}
//		" write locationIdentifier numListings fileOut.txt " +
//			"&is_paid=all&query=car&search_distance_type=mi&sort=rel\n\t\t" +
//			" " +
//			"or\\n\\t\\t* load file.txt" )
	}

	def load ( args : mutable.HashMap[ String, String ] ) : Unit = {
		val closedFile = new PrintWriter ( new BufferedWriter ( new FileWriter ( args.get ( "closedFile" ).get, true ) ) )
		var listings = List [ Listing ]( )
		for ( line <- Source.fromFile ( args.get ( "openFile" ).get ).getLines ( ) ) {
			val lineSegments = line.split ( ";" )
			val newListing = processLineSegment ( lineSegments )
			if ( newListing.getIsDead ( ) ) {
				closedFile.println ( serializeListing ( newListing ) )
			} else {
				listings = newListing :: listings
			}
		}
		closedFile.close ( )
		val openFile = new PrintWriter ( new BufferedWriter ( new FileWriter ( args.get ( "openFile" ).get,
																																					 false ) ) )
		printListings ( listings, openFile )
		println ( "number of listings: " + listings.size )
		openFile.close ( )
	}

	def write ( args : mutable.HashMap[ String, String ] ) : Unit = {
		val closedFile = new PrintWriter ( new BufferedWriter ( new FileWriter ( args.get ( "closedFile" ).get, true ) ) )
		var listings = List [ Listing ]( )
		for ( line <- Source.fromFile ( args.get ( "openFile" ).get ).getLines ( ) ) {
			val lineSegments = line.split ( ";" )
			val newListing = processLineSegment ( lineSegments )
			if ( newListing.getIsDead ( ) ) {
				closedFile.println ( serializeListing ( newListing ) )
			} else {
				listings = newListing :: listings
			}
		}

		val searchPostfix = args.get ( "searchPostfix" ).get
		baseURL = "http://" + args.get ( "location" ).get + baseURL
		val numListings = Integer.parseInt ( args.get ( "numListings" ).get ) - listings.size

		var pageNumber = 0
		while ( listings.size < numListings ) {
			val tempListings = getListings ( baseURL + searchStart + pageNumber * 100 + searchPostfix,
																			 numListings - listings.size )
			pageNumber += 1
			listings = tempListings ::: listings
		}
		println ( "number of listings: " + listings.length )
		val out : PrintWriter =
			new PrintWriter ( new BufferedWriter ( new FileWriter ( args.get ( "openFile" ).get, true ) ) )

		if ( out == null ) {
			printListings ( listings )
		} else {
			printListings ( listings = listings, printWriter = out )
			out.close ( )
			closedFile.close ( )
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
		if ( document.toString.indexOf ( "This posting has been deleted by its author" ) == -1 ) {
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

	def loadConfigFile ( fileLoc : String ) : mutable.HashMap[ String, String ] = {
		val configMap = new mutable.HashMap[ String, String ]( )
		for ( line <- Source.fromFile ( fileLoc ).getLines ( ) ) {
			val lineParts = line.split ( ":" )
			configMap.put ( lineParts ( 0 ), lineParts ( 1 ) )
		}
		return configMap
	}
}
