/**
 * 
 */
package org.snowjak.city.map.tiles

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
	 * Does this rule fit 
	 * @param m
	 * @param cx
	 * @param cy
	 * @return
	 */
	public boolean isAcceptable(int[][] heights) {
		synchronized(this) {
			support.localHeight = heights
			
			spec()
		}
	}
}
