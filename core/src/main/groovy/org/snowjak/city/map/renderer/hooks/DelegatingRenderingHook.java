/**
 * 
 */
package org.snowjak.city.map.renderer.hooks;

import org.snowjak.city.map.renderer.RenderingSupport;
import org.snowjak.city.module.Module;
import org.snowjak.city.module.ModuleExceptionRegistry;
import org.snowjak.city.module.ModuleExceptionRegistry.FailureDomain;

import com.badlogic.gdx.graphics.g2d.Batch;

import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * {@link AbstractMapRenderingHook} that delegates to a
 * {@link RenderingHook} (enabling you to delegate to a lambda-expression
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
public class DelegatingRenderingHook extends AbstractRenderingHook {
	
	private final ModuleExceptionRegistry exceptionRegistry;
	private final Module module;
	private final RenderingHook implementation;
	
	private boolean enabled = true;
	
	/**
	 * Create a new custom-rendering hook with the given {@code id}.
	 * 
	 * @param id
	 * @param implementation
	 */
	public DelegatingRenderingHook(String id, ModuleExceptionRegistry exceptionRegistry, Module module,
			RenderingHook implementation) {
		
		super(id);
		
		this.exceptionRegistry = exceptionRegistry;
		this.module = module;
		this.implementation = implementation;
	}
	
	public Module getModule() {
		
		return module;
	}
	
	@Override
	public boolean isEnabled() {
		
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		
		this.enabled = enabled;
	}
	
	@Override
	public void render(float delta, Batch batch, ShapeDrawer shapeDrawer, RenderingSupport support) {
		
		try {
			implementation.render(delta, batch, shapeDrawer, support);
		} catch (Throwable t) {
			exceptionRegistry.reportFailure(module, FailureDomain.CUSTOM_RENDERER, t);
			enabled = false;
		}
	}
}
