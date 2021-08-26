/**
 * 
 */
package org.snowjak.city.map.renderer.hooks;

import org.snowjak.city.map.renderer.RenderingSupport;

import com.badlogic.gdx.graphics.g2d.Batch;

import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * {@link AbstractMapRenderingHook} that delegates to a
 * {@link CustomRenderingHook} (enabling you to delegate to a lambda-expression
 * or a Closure). The screen-renderer will call all rendering-hooks in ascending
 * order of their {@code priority}.
 * <p>
 * For guidance, the screen-renderer itself has priority <strong>0</strong>.
 * Custom-renderers with a negative priority will be triggered <em>before</em>
 * the normal screen-renderer.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class DelegatingCustomRenderingHook extends AbstractCustomRenderingHook {
	
	private final CustomRenderingHook implementation;
	
	/**
	 * Create a new custom-rendering hook with the given {@code id}.
	 * 
	 * @param id
	 * @param implementation
	 */
	public DelegatingCustomRenderingHook(String id, CustomRenderingHook implementation) {
		
		super(id);
		
		this.implementation = implementation;
	}
	
	@Override
	public void render(float delta, Batch batch, ShapeDrawer shapeDrawer, RenderingSupport support) {
		
		implementation.render(delta, batch, shapeDrawer, support);
	}
}
