/**
 * 
 */
package org.snowjak.city.map.tiles.support

import org.snowjak.city.map.tiles.TileCorner

/**
 * Allows you to query and mutate the map.
 * 
 * @author snowjak88
 *
 */
class MutatorTileSupport extends TileSupport {
	
	/**
	 * Override the list of flavors associated with the given corner of the
	 * currently-considered cell.
	 * @param corner
	 * @param flavors
	 */
	public void setFlavor(TileCorner corner, String...flavors) {
		setFlavor 0, 0, corner, flavors
	}
	
	/**
	 * Override the list of flavors associated with the given corner of the cell
	 * located {@code dx,dy} away from the currently-considered cell.
	 * @param dx
	 * @param dy
	 * @param corner
	 * @param flavors
	 */
	public void setFlavor(int dx, int dy, TileCorner corner, String...flavors) {
		if(!map.isValidCell(cellX + dx, cellY + dy))
			return
		
		map.setTileCornerFlavors cellX + dx, cellY + dy, corner, flavors.toList()
	}
	
	/**
	 * Add the given flavor(s) to the given corner of the currently-considered cell.
	 * @param corner
	 * @param flavors
	 */
	public void addFlavor(TileCorner corner, String...flavors) {
		addFlavor 0, 0, corner, flavors
	}
	
	/**
	 * Add the given flavor(s) to the given corner of the cell located {@code dx,dy} away from the currently-considered cell.
	 * @param dx
	 * @param dy
	 * @param corner
	 * @param flavors
	 */
	public void addFlavor(int dx, int dy, TileCorner corner, String...flavors) {
		if(!map.isValidCell(cellX + dx, cellY + dy))
			return
		
		def newFlavors = new ArrayList(map.getTileCornerFlavors(cellX + dx, cellY + dy, corner))
		newFlavors.addAll flavors
		map.setTileCornerFlavors cellX + dx, cellY + dy, corner, newFlavors
	}
	
	/**
	 * Remove the given flavor(s) from the given corner of the currently-considered cell.
	 * @param corner
	 * @param flavors
	 */
	public void removeFlavor(TileCorner corner, String...flavors) {
		removeFlavor 0, 0, corner, flavors
	}
	
	/**
	 * Remove the given flavor(s) from the given corner of the cell located {@code dx,dy} away from the currently-considered cell.
	 * @param dx
	 * @param dy
	 * @param corner
	 * @param flavors
	 */
	public void removeFlavor(int dx, int dy, TileCorner corner, String...flavors) {
		if(!map.isValidCell(cellX + dx, cellY + dy))
			return
		
		def newFlavors = new ArrayList(map.getTileCornerFlavors(cellX + dx, cellY + dy, corner))
		newFlavors.removeAll flavors
		map.setTileCornerFlavors cellX + dx, cellY + dy, corner, newFlavors
	}
	
	/**
	 * Set the altitude for the given corner of the currently-considered cell.
	 * @param corner
	 * @param altitude
	 */
	public void setAltitude(TileCorner corner, int altitude) {
		setAltitude 0, 0, corner, altitude
	}
	
	/**
	 * Set the altitude for the given corner of the cell located {@code dx,dy} away from the currently-considered cell.
	 * @param dx
	 * @param dy
	 * @param corner
	 * @param altitude
	 */
	public void setAltitude(int dx, int dy, TileCorner corner, int altitude) {
		if(!map.isValidCell(cellX + dx, cellY + dy))
			return
		
		map.setTileCornerAltitude cellX + dy, cellY + dy, corner, altitude
	}
}
