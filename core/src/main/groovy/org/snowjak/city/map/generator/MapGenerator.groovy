/**
 * 
 */
package org.snowjak.city.map.generator

import org.snowjak.city.map.CityMap
import org.snowjak.city.map.generator.support.FlavorsProducer
import org.snowjak.city.map.generator.support.MapGeneratorDsl

import com.sudoplay.joise.module.Module

/**
 * @author snowjak88
 *
 */
class MapGenerator {
	
	private MapGeneratorDsl gen;
	
	public MapGenerator(MapGeneratorDsl definition) {
		this.gen = definition;
	}
	
	public void setSeed(String seed) {
		this.gen.setSeed(seed);
	}
	
	/**
	 * Run this generator, producing a new map with the given dimensions in tiles/cells.
	 * <p>
	 * Populates the map with vertex-altitudes and -flavors, and then executes 
	 * </p>
	 * 
	 * @param width
	 * @param height
	 * @param wrapX
	 * @param wrapY
	 * @return
	 */
	public CityMap generate(int width, int height, boolean wrapX = false, boolean wrapY = false) {
		
		if(gen == null)
			throw new NullPointerException()
		
		final Module altitudeProducer = gen.altitude
		final FlavorsProducer flavorProducer = gen.flavors
		
		final CityMap map = new CityMap(width, height)
		
		for(int y in 0..height) {
			for(int x in 0..width) {
				def altitude = (int) altitudeProducer.get(x,y)
				List<String> flavors = flavorProducer.get(x,y)
				
				map.setVertexAltitude x, y, altitude
				map.setVertexFlavors x, y, flavors
			}
		}
		
		map
	}
}
