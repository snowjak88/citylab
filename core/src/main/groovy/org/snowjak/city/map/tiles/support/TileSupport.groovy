/**
 * 
 */
package org.snowjak.city.map.tiles.support

import org.snowjak.city.map.tiles.TileCorner

class TileSupport {
	
	/**
	 * Holds the height-values for each of the tile's corners.
	 * Can be addressed using {@link TileCorner#offsetX}, {@link TileCorner#offsetY}
	 */
	int[][] localHeight = new int[2][2]
	
	/**
	 * Holds "extra" properties which can be examined by a tile-rule
	 */
	Expando ext = new Expando()
	
	/**
	 * returns {@code true} if the two Collections match -- i.e., if, for each element in one collection, it
	 * <a href="http://www.groovy-lang.org/operators.html#_identity_operator">equals</a> an element in the other.
	 * @param c1
	 * @param c2
	 * @return
	 */
	public boolean listsMatch(Collection<?> c1, Collection<?> c2) {
		if(c1 == c2)
			return true
		if(c1 == null || c2 == null)
			return false
		if( c1?.any { !(c2?.contains( it )) } )
			return false
		if( c2?.any { !(c1?.contains( it )) } )
			return false
		true
	}
	
	/**
	 * @return {@code true} if all this tile's corners need to be at the same altitude
	 */
	public boolean isFlat() {
		isFlat([
			TileCorner.TOP,
			TileCorner.BOTTOM,
			TileCorner.LEFT,
			TileCorner.RIGHT
		])
	}
	
	/**
	 * @param corners
	 * @return {@code true} if all of the given corners of this tile need to be at the same altitude
	 */
	public boolean isFlat(List<TileCorner> corners) {
		if(corners.isEmpty())
			return isFlat()
		
		if(corners.size() == 1)
			return true
		
		def firstAltitude = alt(corners.first())
		for(TileCorner corner : corners)
			if(alt(corner) != firstAltitude)
				return false
		
		true
	}
	
	/**
	 * Checks if the {@code otherCorners} all have the same altitude relative to the given {@code origin} corner.
	 * <p>
	 * If an {@code other} corner is 1 level higher than {@code origin}, then {@code delta == +1}.
	 * </p>
	 * @param origin
	 * @param otherCorners
	 * @param expectedDelta
	 * @return
	 */
	public boolean altDelta(TileCorner origin, List<TileCorner> otherCorners, int expectedDelta) {
		def originAltitude = alt(origin)
		
		for(TileCorner other : otherCorners) {
			def delta = alt(other) - originAltitude
			if(delta != expectedDelta)
				return false
		}
		
		true
	}
	
	/**
	 * Get the altitude at the given corner for the currently-considered cell.
	 * @param corner
	 * @return
	 */
	public int alt(TileCorner corner) {
		
		localHeight[corner.offsetX][corner.offsetY]
	}
}
