/**
 * 
 */
package org.snowjak.city.map.renderer.hooks;

import org.snowjak.city.util.Prioritized;

/**
 * @author snowjak88
 *
 */
public abstract class AbstractCustomRenderingHook implements CustomRenderingHook, Prioritized {
	
	private final int priority;
	
	public AbstractCustomRenderingHook(int priority) {
		
		this.priority = priority;
	}
	
	@Override
	public int getPriority() {
		
		return priority;
	}
}
