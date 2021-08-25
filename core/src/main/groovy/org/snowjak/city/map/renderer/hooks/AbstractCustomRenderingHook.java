/**
 * 
 */
package org.snowjak.city.map.renderer.hooks;

import org.snowjak.city.util.RelativePriority;
import org.snowjak.city.util.RelativelyPrioritized;

/**
 * @author snowjak88
 *
 */
public abstract class AbstractCustomRenderingHook
		implements CustomRenderingHook, RelativelyPrioritized<AbstractCustomRenderingHook, String> {
	
	private final String id;
	private final RelativePriority<String> relativePriority;
	
	public AbstractCustomRenderingHook(String id) {
		
		this.id = id;
		this.relativePriority = new RelativePriority<>();
	}
	
	public String getId() {
		
		return id;
	}
	
	@Override
	public String getRelativePriorityKey() {
		
		return id;
	}
	
	@Override
	public RelativePriority<String> getRelativePriority() {
		
		return relativePriority;
	}
}
