package mknutsen.craiglist.parser

import java.io.{BufferedWriter, FileWriter, PrintWriter}

import org.jsoup.Jsoup

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
		* [write or load,
		* city in a string (washingtondc),
		* number of pages to look through (3),
		* file to write to (file.txt),
		* search query taken from searching in craiglist and c+p it in
		* (&is_paid=all&query=car&search_distance_type=mi&sort=rel)]
		*/
	def main ( args : Array[ String ] ) : Unit = {
		args ( 0 ) match {
			case "write" ⇒ write ( args )
			case "load" ⇒ load ( args )
			case _ ⇒ throw new IllegalArgumentException ( "[write or load, \ncity in a string (washingtondc), \nnumber of " +
																											"pages " +
																											"to look through (3), \nfile to write to (file.txt), \nsearch " +
																											"query " +
																											"taken from searching in craiglist and c+p it in \n" +
																											"(&is_paid=all&query=car&search_distance_type=mi&sort=rel)]" )
		}
	}

	def load ( args : Array[ String ] ) : Unit = {
		var listings = List [ Listing ]( )
		for ( line <- Source.fromFile ( args ( 1 ) ).getLines ( ) ) {
			val lineSegments = line.split ( ";" )
			val newListing = processLineSegment ( lineSegments )
			println ( newListing )
			listings = newListing :: listings
		}
		print ( listings )
	}

	def write ( args : Array[ String ] ) : Unit = {
		val searchPostfix = if ( args.length > 4 ) args ( 4 ) else ""
		baseURL = "http://" + args ( 1 ) + baseURL
		val numPages = if ( args.length >= 3 ) Integer.parseInt ( args ( 2) ) else 1
		var listings : List[ Listing ] = List ( )
		for ( i <- 0 to numPages - 1 ) {
			val tempListings = getListings ( baseURL + searchStart + i * 100 + searchPostfix )
			listings = tempListings ::: listings
		}
		println ( "number of listings: " + listings.length )
		val out : PrintWriter = if ( args.length > 3 ) {
			new PrintWriter ( new BufferedWriter ( new FileWriter ( args ( 3 ),
																															false ) ) )
		} else {
			null
		}
		for ( listing <- listings ) {
			println ( listing )
			if ( out != null ) out.println ( serializeListing ( listing ) )
		}
		if ( out != null ) out.close ( )
	}

	def getListings ( url : String ) : List[ Listing ] = {
		val elems = Listing.getElements ( url, ".i" )
		while ( elems.size ( ) > 15 ) {
			elems.remove ( 0 )
		}
		val links = Listing.getUrls ( elems )
		for ( link <- links ) yield new Listing ( baseURL + link, Jsoup.connect ( baseURL + link ).get ( ) )

	}

	def processLineSegment ( lineSegments : Array[ String ] ) : Listing = {
		val document = Jsoup.connect ( lineSegments ( 1 ) ).get ( )
		if ( document.toString.indexOf ( "Not Found" ) == -1 ) {
			return new Listing ( lineSegments ( 1 ), Jsoup.connect ( lineSegments ( 1 ) ).get ( ) )
		} else {
			return new Listing ( lineSegments ( 1 ), true, "", "", null, null, -1, "" ) // to implement later
		}
	}

	def serializeListing ( listing : Listing ) : String = {
		if ( listing.getIsDead ( ) ) "" else listing.getTitle ( ) + ";" + listing.getURL ( )
	}
}
