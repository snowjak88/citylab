/**
 * 
 */
package org.snowjak.city.map.renderer.hooks;

import org.snowjak.city.map.renderer.RenderingSupport;

import com.badlogic.gdx.graphics.g2d.Batch;

import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * @author snowjak88
 *
 */
@FunctionalInterface
public interface RenderingHook {
	
	public void render(float delta, Batch batch, ShapeDrawer shapeDrawer, RenderingSupport support);
	
	/**
	 * @return {@code true} if this rendering-hook is eligible to be executed
	 */
	public default boolean isEnabled() {
		return true;
	}
}
