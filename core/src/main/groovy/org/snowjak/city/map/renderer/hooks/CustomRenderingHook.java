/**
 * 
 */
package org.snowjak.city.map.renderer.hooks;

import org.snowjak.city.map.renderer.RenderingSupport;

/**
 * @author snowjak88
 *
 */
@FunctionalInterface
public interface CustomRenderingHook {
	
	public void render(RenderingSupport support);
}
