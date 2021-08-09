/**
 * 
 */
package org.snowjak.city.map.tiles

import org.snowjak.city.map.CityMap
import org.snowjak.city.map.tiles.support.MutatorTileSupport

/**
 * @author snowjak88
 *
 */
class MapMutator extends TileRule<MutatorTileSupport> {
		
	
	
	MapMutator(Closure spec, Map<String, Closure> helpers, MutatorTileSupport support) {
		
		super(spec, helpers, support);
	}

	/**
	 * Execute this mutator against the given cell of the given map.
	 * @param map
	 * @param cellX
	 * @param cellY
	 */
	public void mutate(CityMap map, int cellX, int cellY) {
		support.cellX = cellX
		support.cellY = cellY
		support.map = map
		spec()
	}
}
