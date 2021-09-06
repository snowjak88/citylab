/**
 * 
 */
package org.snowjak.city.map.renderer.hooks;

import org.snowjak.city.map.renderer.RenderingSupport;
import org.snowjak.city.module.Module;
import org.snowjak.city.module.ModuleExceptionRegistry;
import org.snowjak.city.module.ModuleExceptionRegistry.FailureDomain;

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
	
	private final ModuleExceptionRegistry exceptionRegistry;
	private final Module module;
	private final CellRenderingHook implementation;
	
	private boolean enabled = true;
	
	public DelegatingCellRenderingHook(String id, ModuleExceptionRegistry exceptionRegistry, Module module,
			CellRenderingHook implementation) {
		
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
	public void renderCell(float delta, int cellX, int cellY, RenderingSupport support) {
		
		try {
			implementation.renderCell(delta, cellX, cellY, support);
		} catch (Throwable t) {
			exceptionRegistry.reportFailure(module, FailureDomain.CELL_RENDERER, t);
			enabled = false;
		}
	}
}
