package mknutsen.craiglist.parser

import org.jsoup.Jsoup

import scala.io.Source

/**
	* Created by mknutsen on 1/15/16.
	*/
object ListingLoader {
	def processLineSegment ( lineSegments : Array[ String ] ) : Listing = {
		val document = Jsoup.connect ( lineSegments ( 2 ) ).get ( )
		if ( document.toString.indexOf ( "Not Found" ) > -1 ) {
			return new Listing ( lineSegments ( 2 ), Jsoup.connect ( lineSegments ( 2 ) ).get ( ) )
		} else {
			return new Listing ( lineSegments ( 2 ), true, "", "", null, null, -1, "" ) // to implement later
		}
	}

	def serializeListing ( listing : Listing ) : String = {
		if ( listing.getIsDead ( ) ) "" else listing.getTitle ( ) + ";" + listing.getURL ( )
	}

	def main ( args : Array[ String ] ) : Unit = {
		var listings = List [ Listing ]( )
		for ( line <- Source.fromFile ( args ( 0 ) ).getLines ( ) ) {
			val lineSegments = line.split ( ";" )
			val newListing = ListingLoader.processLineSegment ( lineSegments )
			println ( newListing )
			listings = newListing :: listings
		}
		print ( listings )
	}
}
