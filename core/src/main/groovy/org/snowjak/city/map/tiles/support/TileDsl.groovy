/**
 * 
 */
package org.snowjak.city.map.tiles.support

import org.snowjak.city.map.tiles.TileCorner

/**
 * @author snowjak88
 *
 */
class TileDsl {
	private TileSetDsl tsd
	
	String id
	int x, y
	int width, height
	int gridWidth, gridHeight
	int padding, surfaceOffset, altitudeOffset
	String filename
	TileCorner base
	
	boolean decoration = false
	
	Map<TileCorner,List<String>> provision = [:]
	List<Closure> rules = []
	Map<String,Closure> ruleHelpers = [:]
	
	/**
	 * Denotes that this tile provides the given flavors to all corners.
	 * <p>
	 * Note that successive {@code provides()} calls are additive upon one another.
	 * </p>
	 * @param provision
	 */
	public void provides(String... provision) {
		provides([
			TileCorner.TOP,
			TileCorner.RIGHT,
			TileCorner.BOTTOM,
			TileCorner.LEFT
		], provision.toList())
	}
	
	/**
	 * Denotes that this tile provides the given flavors to the following corners.
	 * <p>
	 * Note that successive {@code provides()} calls are additive upon one another.
	 * </p>
	 * @param corners
	 * @param provision
	 */
	public void provides(List<TileCorner> corners, List<String> provision) {
		corners.each { corner ->
			this.provision.computeIfAbsent(corner, {c -> new LinkedList<>(provision)})
		}
	}
	
	/**
	 * Define a rule for this tile. For a tile to be considered for placement on the map, every one of its defined rules must be met.
	 * @param rule
	 */
	public void rule(@DelegatesTo(TileSupport) Closure rule) {
		rules << rule
	}
}