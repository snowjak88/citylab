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
public enum MapLayer {
	
	/**
	 * First (bottom) layer. Grass, dirt, that kind of thing.
	 */
	TERRAIN,
	/**
	 * Drawn on top of terrain, under roads.
	 */
	WATER,
	/**
	 * Roads, rails, pipes, everything that goes on top of terrain + water.
	 */
	ROAD,
	/**
	 * Decorative elements fallen on the ground
	 */
	DECORATION,
	/**
	 * Plants growing from the ground
	 */
	VEGETATION,
	/**
	 * Buildings
	 */
	STRUCTURE;
	
}
