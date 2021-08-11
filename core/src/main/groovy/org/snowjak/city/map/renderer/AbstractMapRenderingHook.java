/**
 * 
 */
package org.snowjak.city.map.renderer;

import org.snowjak.city.map.CityMap;

/**
 * Allows you to customize what gets rendered as part of a {@link CityMap} cell.
 * 
 * @author snowjak88
 *
 */
public abstract class AbstractMapRenderingHook implements MapRenderingHook {
	
	private final int order;
	
	public AbstractMapRenderingHook(int order) {
		
		this.order = order;
	}
	
	public int getOrder() {
		
		return order;
	}
	
	@Override
	public abstract void renderCell(int cellX, int cellY, RenderingSupport support);
}
