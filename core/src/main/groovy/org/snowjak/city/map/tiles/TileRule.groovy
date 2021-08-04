/**
 * 
 */
package org.snowjak.city.map.tiles

import org.snowjak.city.map.CityMap
import org.snowjak.city.map.tiles.support.TileRuleSupport

/**
 * @author snowjak88
 *
 */
class TileRule {
	
	private Closure ruleSpec
	private TileRuleSupport support

	public TileRule(Closure ruleSpec, TileRuleSupport support) {
		
		this.ruleSpec = ruleSpec.rehydrate(support, this, this)
		this.ruleSpec.resolveStrategy = Closure.DELEGATE_FIRST
		
		this.support = support
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
		
		ruleSpec()
	}
}
