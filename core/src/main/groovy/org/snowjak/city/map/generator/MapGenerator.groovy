/**
 * 
 */
package org.snowjak.city.map.generator

import java.util.function.DoubleConsumer

import org.snowjak.city.map.CityMap
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
	
	public String getTitle() {
		gen.title
	}
	
	public String getDescription() {
		gen.description
	}
	
	/**
	 * Run this generator, producing a new map with the given dimensions in tiles/cells.
	 * <p>
	 * Populates the map with vertex-altitudes and -flavors, and then executes 
	 * </p>
	 * 
	 * @param width
	 * @param height
	 * @param progressUpdater optional progress-reporter. Called on every map-vertex with values in [0,1]
	 * @return
	 */
	public CityMap generate(int width, int height, DoubleConsumer progressUpdater = {p -> }) {
		
		if(gen == null)
			throw new NullPointerException()
		
		final Module altitudeProducer = gen.altitude
		
		final CityMap map = new CityMap(width, height)
		
		progressUpdater.accept 0
		
		for(int y in 0..height) {
			for(int x in 0..width) {
				
				def progress = ((double)x / (double)width + (double)y / (double)height) / 2.0
				progressUpdater.accept progress
				
				def altitude = (int) altitudeProducer.get(x,y)
				
				map.setVertexAltitude x, y, altitude
			}
		}
		
		map
	}
}
