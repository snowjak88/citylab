/**
 * 
 */
package org.snowjak.city.map;

/**
 * Defines the different layers which make up a game map.
 * 
 * @author snowjak88
 *
 */
public enum MapDomain {
	
	/**
	 * First (bottom) layer. Grass, dirt, that kind of thing.
	 */
	TERRAIN(1, "/images/tilesets/terrain/"),
	/**
	 * Second layer. Drawn on top of terrain, under roads.
	 */
	WATER(2, "/images/tilesets/water/"),
	/**
	 * Third layer. Roads, rails, pipes, everything that goes on top of terrain +
	 * water.
	 */
	ROAD(3, "/images/tilesets/road/"),
	/**
	 * Fourth layer.
	 */
	STRUCTURE(4, "/images/tilesets/structure/");
	
	private final String baseDirectoryName;
	private final int order;
	
	private MapDomain(int order, String baseDirectoryName) {
		
		this.order = order;
		this.baseDirectoryName = baseDirectoryName;
	}
	
	public int getOrder() {
		
		return order;
	}
	
	/**
	 * Get the directory under which we expect to find all tile-sets associated with
	 * this MapLayer.
	 */
	public String getBaseDirectoryName() {
		
		return baseDirectoryName;
	}
}
