/**
 * 
 */
package org.snowjak.city.map.generator
import static org.snowjak.city.map.Map.DIMENSION_ALTITUDE
import static org.snowjak.city.map.MapLayer.TERRAIN
import static org.snowjak.city.util.Util.min

import org.snowjak.city.map.BoundedMap
import org.snowjak.city.map.MapLayer
import org.snowjak.city.map.generator.support.MapGeneratorSpec
import org.snowjak.city.map.generator.support.MaterialProducer
import org.snowjak.city.map.tiles.TileDescriptor
import org.snowjak.city.map.tiles.TileSet

import com.sudoplay.joise.module.Module

/**
 * @author snowjak88
 *
 */
class MapGenerator {
	
	private static final Random RND = new Random(System.currentTimeMillis());
	
	public org.snowjak.city.map.Map generateBounded(int width, int height, MapGeneratorSpec gen, TileSet terrainTileset, boolean wrapX = false, boolean wrapY = false) {
		
		if(gen == null || terrainTileset == null)
			throw new NullPointerException()
		
		final Module altitudeProducer = gen.altitude;
		final MaterialProducer materialProducer = gen.material
		
		final org.snowjak.city.map.Map map = new BoundedMap(width, height)
		map.setTileSetFor MapLayer.TERRAIN, terrainTileset
		
		def altitudes = new int[width+1][height+1]
		def materials = new String[width+1][height+1]
		
		for(int x in 0..width)
			for(int y in 0..height) {
				altitudes[x][y] = altitudeProducer.get(x, y)
				materials[x][y] = materialProducer.get(x, y)
			}
		
		
		def tileDescriptors = new TileDescriptor[width][height]
		for(int y in 0..height) {
			print "[ "
			for(int x in 0..width)
				print "${altitudes[x][y]} "
			println "]"
		}
		
		for(int x in 0..width-1)
			for(int y in 0..height-1) {
				def possibilities = terrainTileset.findDescriptorsThatFit(altitudes, x, y, wrapX, wrapY, materials)
				
				if(possibilities.isEmpty())
					println """ERROR: Cannot find tile to fit:
Altitude: ${altitudes[x][y]} ${altitudes[x+1][y]}
          ${altitudes[x+1][y]} ${altitudes[x+1][y+1]}
Material: ${materials[x][y]} ${materials[x+1][y]}
          ${materials[x+1][y]} ${materials[x+1][y+1]}"""
				else
					tileDescriptors[x][y] = possibilities[RND.nextInt(possibilities.size())]
			}
		
		mixUpTileAssignments altitudes, materials, tileDescriptors, terrainTileset, wrapX, wrapY
		
		for(int y in 0..height-1) {
			for(int x in 0..width-1) {
				if(tileDescriptors[x][y] == null)
					continue
				
				def tileHashcode = tileDescriptors[x][y].hashcode
				def minAltitude = min(altitudes[x][y], altitudes[x+1][y], altitudes[x][y+1], altitudes[x+1][y+1])
				
				map.setCell x, y, TERRAIN, tileHashcode
				map.setCell x, y, DIMENSION_ALTITUDE, minAltitude
			}
		}
		
		map
	}
	
	private void mixUpTileAssignments(int[][] altitudes, String[][] materials, TileDescriptor[][] descriptors, TileSet tileset, boolean wrapX, boolean wrapY) {
		def width = descriptors.length
		def height = descriptors[0].length
		
		for(int x in (0..width-1))
			for(int y in (0..height-1)) {
				
				def possibilities = []
				possibilities.addAll tileset.findDescriptorsThatFit(altitudes, x, y, wrapX, wrapY, materials)
				
				def validities = []
				
				def oldAssignment = descriptors[x][y]
				for(TileDescriptor possibility : possibilities) {
					
					descriptors[x][y] = possibility
					
					if(tileset.isTileFitting(x, y, descriptors, altitudes, materials, wrapX, wrapY))
						validities << descriptors[x][y]
					
					descriptors[x][y] = oldAssignment
				}
				
				if(!validities.isEmpty())
					descriptors[x][y] = validities[RND.nextInt(validities.size())]
			}
	}
}
