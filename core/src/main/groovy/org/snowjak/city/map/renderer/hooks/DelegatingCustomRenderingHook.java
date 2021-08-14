/**
 * 
 */
package org.snowjak.city.map.renderer.hooks;

import org.snowjak.city.map.renderer.RenderingSupport;

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
	 * Create a new custom-rendering hook with the given {@code priority}. The
	 * screen-renderer will call all rendering-hooks in order of their
	 * {@code priority}.
	 * <p>
	 * For guidance, the screen-renderer itself has priority <strong>0</strong>.
	 * Custom-renderers with a negative priority will be triggered <em>before</em>
	 * the normal screen-renderer.
	 * </p>
	 * 
	 * @param priority
	 * @param implementation
	 */
	public DelegatingCustomRenderingHook(int priority, CustomRenderingHook implementation) {
		
		super(priority);
		
		this.implementation = implementation;
	}
	
	@Override
	public void render(RenderingSupport support) {
		
		implementation.render(support);
	}
}
