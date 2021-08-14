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
public interface CustomRenderingHook {
	
	public void render(Batch batch, ShapeDrawer shapeDrawer, RenderingSupport support);
}
