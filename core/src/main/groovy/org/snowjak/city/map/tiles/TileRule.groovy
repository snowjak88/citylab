/**
 * 
 */
package org.snowjak.city.map.tiles

import org.snowjak.city.map.CityMap
import org.snowjak.city.map.tiles.support.TileSupport

/**
 * @author snowjak88
 *
 */
class TileRule<S extends TileSupport> {
	
	Closure spec
	S support

	public TileRule(Closure spec, Map<String,Closure> helpers, S support) {
		
		this.spec = spec.rehydrate(support, this, this)
		this.spec.resolveStrategy = Closure.DELEGATE_FIRST
		
		this.support = support
		helpers.each { name, helper ->
			helper = helper.rehydrate(this.support, this.spec, this.spec)
			this.support.metaClass."$name" = helper
		}
	}
	
	/**
	 * Does this rule fit in the given CityMap, at the given cell-coordinates {@code cx,cy}?
	 * @param m
	 * @param cx
	 * @param cy
	 * @return
	 */
	public boolean isAcceptable(CityMap map, int cellX, int cellY) {
		support.map = map
		support.cellX = cellX
		support.cellY = cellY
		
		spec()
	}
}
