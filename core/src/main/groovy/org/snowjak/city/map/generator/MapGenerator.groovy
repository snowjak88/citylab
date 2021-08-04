/**
 * 
 */
package org.snowjak.city.map.generator

import org.snowjak.city.map.CityMap
import org.snowjak.city.map.generator.support.FlavorsProducer
import org.snowjak.city.map.generator.support.MapGeneratorDsl
import org.snowjak.city.map.tiles.TileSet

import com.sudoplay.joise.module.Module

/**
 * @author snowjak88
 *
 */
class MapGenerator {
	
	private static final Random RND = new Random(System.currentTimeMillis());
	
	private MapGeneratorDsl gen;
	
	public MapGenerator(MapGeneratorDsl definition) {
		this.gen = definition;
	}
	
	/**
	 * Run this generator, producing a new map with the given dimensions in tiles/cells.
	 * <p>
	 * Populates the map with vertex-altitudes and -flavors, and then executes 
	 * </p>
	 * 
	 * @param width
	 * @param height
	 * @param tileset
	 * @param wrapX
	 * @param wrapY
	 * @return
	 */
	public CityMap generate(int width, int height, TileSet tileset, boolean wrapX = false, boolean wrapY = false) {
		
		if(gen == null || tileset == null)
			throw new NullPointerException()
		
		final Module altitudeProducer = gen.altitude;
		final FlavorsProducer flavorProducer = gen.flavors
		
		final CityMap map = new CityMap(width, height)
		
		for(int y in 0..height) {
			print "[ "
			for(int x in 0..width) {
				def altitude = (int) altitudeProducer.get(x,y).round()
				print "$altitude "
				map.setVertexAltitude x, y, altitude
				map.setVertexFlavors x, y, flavorProducer.get(x,y)
			}
			println "]"
		}
		
		map.updateTiles tileset
		
		map
	}
}
