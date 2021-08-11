/**
 * 
 */
package org.snowjak.city.map.renderer;

@FunctionalInterface
public interface MapRenderingHook {
	public void renderCell(int cellX, int cellY, RenderingSupport support);
}