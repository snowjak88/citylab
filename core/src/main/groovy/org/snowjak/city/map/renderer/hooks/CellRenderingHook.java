/**
 * 
 */
package org.snowjak.city.map.renderer.hooks;

import org.snowjak.city.map.renderer.RenderingSupport;

/**
 * Describes a custom rendering-hook that can render to whatever cell is
 * currently being rendered.
 * 
 * @author snowjak88
 *
 */
@FunctionalInterface
public interface CellRenderingHook {
	
	public void renderCell(int cellX, int cellY, RenderingSupport support);
}