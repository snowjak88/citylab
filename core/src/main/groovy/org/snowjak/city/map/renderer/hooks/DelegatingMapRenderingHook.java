/**
 * 
 */
package org.snowjak.city.map.renderer.hooks;

import org.snowjak.city.map.renderer.MapRenderingHook;
import org.snowjak.city.map.renderer.RenderingSupport;

/**
 * {@link AbstractMapRenderingHook} that delegates to a {@link MapRenderingHook}
 * (enabling you to delegate to a lambda-expression or a Closure).
 * 
 * @author snowjak88
 *
 */
public class DelegatingMapRenderingHook extends AbstractMapRenderingHook {
	
	private final MapRenderingHook implementation;
	
	public DelegatingMapRenderingHook(int order, MapRenderingHook implementation) {
		
		super(order);
		
		this.implementation = implementation;
	}
	
	@Override
	public void renderCell(int cellX, int cellY, RenderingSupport support) {
		
		implementation.renderCell(cellX, cellY, support);
	}
	
}
