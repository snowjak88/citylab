/**
 * 
 */
package org.snowjak.city.map.renderer.hooks;

import org.snowjak.city.util.Prioritized;

/**
 * @author snowjak88
 *
 */
public abstract class AbstractCellRenderingHook implements CellRenderingHook, Prioritized {
	
	private final int priority;
	
	public AbstractCellRenderingHook(int priority) {
		
		this.priority = priority;
	}
	
	@Override
	public int getPriority() {
		
		return priority;
	}
}
