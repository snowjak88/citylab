/**
 * 
 */
package org.snowjak.city.map.renderer.hooks;

import org.snowjak.city.map.renderer.RenderingSupport;

/**
 * {@link AbstractMapRenderingHook} that delegates to a
 * {@link CellRenderingHook} (enabling you to delegate to a lambda-expression or
 * a Closure). When rendering on-screen map-cells, the screen-renderer will call
 * all {@link CellRenderingHook}s in ascending order of their {@code priority}.
 * 
 * @author snowjak88
 *
 */
public class DelegatingCellRenderingHook extends AbstractCellRenderingHook {
	
	private final CellRenderingHook implementation;
	
	public DelegatingCellRenderingHook(String id, CellRenderingHook implementation) {
		
		super(id);
		
		this.implementation = implementation;
	}
	
	@Override
	public void renderCell(int cellX, int cellY, RenderingSupport support) {
		
		implementation.renderCell(cellX, cellY, support);
	}
}
