/**
 * 
 */
package org.snowjak.city.map.tiles.support

import org.snowjak.city.map.CityMap
import org.snowjak.city.map.tiles.TileCorner

/**
 * Allows you to query the map.
 * 
 * @author snowjak88
 *
 */
class TileSupport {
	
	CityMap map
	int cellX, cellY
	
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
		alt(0, 0, corner)
	}
	
	/**
	 * Get the altitude on the given corner for the cell located {@code dx,dy} away from the currently-considered cell.
	 * @param dx
	 * @param dy
	 * @param corner
	 * @return
	 */
	public int alt(int dx, int dy, TileCorner corner) {
		if(!map.isValidCell(cellX + dx, cellY + dy))
			return map.getTileAltitude(cellX, cellY, corner)
		
		map.getTileAltitude(cellX + dx, cellY + dy, corner)
	}
	
	/**
	 * Returns {@code true} if the given corner for the currently-considered cell includes the given flavor.
	 * @param corner
	 * @param flavor
	 * @return
	 */
	public boolean hasFlavor(TileCorner corner, String flavor) {
		hasFlavor(0, 0, corner, flavor)
	}
	
	/**
	 * Returns {@code true} if the given corner for the cell located {@code dx,dy} away from the currently-considered cell includes the given flavor.
	 * @param dx
	 * @param dy
	 * @param corner
	 * @param flavor
	 * @return
	 */
	public boolean hasFlavor(int dx, int dy, TileCorner corner, String flavor) {
		if(!map.isValidCell(cellX + dx, cellY + dy))
			return map.getTileCornerFlavors(cellX, cellY, corner).contains(flavor)
		
		map.getTileCornerFlavors(cellX + dx, cellY + dy, corner).contains(flavor)
	}
}
