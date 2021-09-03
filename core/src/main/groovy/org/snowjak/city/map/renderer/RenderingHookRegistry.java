package org.snowjak.city.map.renderer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.snowjak.city.map.renderer.hooks.AbstractCellRenderingHook;
import org.snowjak.city.map.renderer.hooks.AbstractCustomRenderingHook;
import org.snowjak.city.util.PrioritizationFailedException;
import org.snowjak.city.util.RelativePriorityList;

/**
 * Serves as a registry for rendering-hooks.
 * 
 * @author snowjak88
 *
 */
public class RenderingHookRegistry {
	
	private final Map<String, AbstractCellRenderingHook> cellRenderingHooks = new LinkedHashMap<>();
	private final RelativePriorityList<String, AbstractCellRenderingHook> prioritizedCellRenderingHooks = new RelativePriorityList<>();
	
	private final Map<String, AbstractCustomRenderingHook> customRenderingHooks = new LinkedHashMap<>();
	private final RelativePriorityList<String, AbstractCustomRenderingHook> prioritizedCustomRenderingHooks = new RelativePriorityList<>();
	
	public List<AbstractCellRenderingHook> getPrioritizedCellRenderingHooks() {
		
		return prioritizedCellRenderingHooks;
	}
	
	public List<AbstractCustomRenderingHook> getPrioritizedCustomRenderingHooks() {
		
		return prioritizedCustomRenderingHooks;
	}
	
	public void addCellRenderingHook(AbstractCellRenderingHook hook) throws PrioritizationFailedException {
		
		final AbstractCellRenderingHook previous = cellRenderingHooks.put(hook.getId(), hook);
		
		if (previous != null)
			prioritizedCellRenderingHooks.remove(previous);
		
		try {
			
			prioritizedCellRenderingHooks.add(hook);
			
		} catch (RuntimeException e) {
			
			if (!(e.getCause() instanceof PrioritizationFailedException))
				throw e;
			
			if (previous == null)
				cellRenderingHooks.remove(hook.getId());
			else {
				cellRenderingHooks.put(hook.getId(), previous);
				prioritizedCellRenderingHooks.add(previous);
			}
			
			throw (PrioritizationFailedException) e.getCause();
		}
	}
	
	public void removeCellRenderingHook(AbstractCellRenderingHook hook) {
		
		cellRenderingHooks.remove(hook.getId());
		prioritizedCellRenderingHooks.remove(hook);
	}
	
	public void addCustomRenderingHook(AbstractCustomRenderingHook hook) throws PrioritizationFailedException {
		
		final AbstractCustomRenderingHook previous = customRenderingHooks.put(hook.getId(), hook);
		
		if (previous != null)
			prioritizedCustomRenderingHooks.remove(previous);
		
		try {
			
			prioritizedCustomRenderingHooks.add(hook);
			
		} catch (RuntimeException e) {
			
			if (!(e.getCause() instanceof PrioritizationFailedException))
				throw e;
			
			if (previous == null)
				customRenderingHooks.remove(hook.getId());
			else {
				customRenderingHooks.put(hook.getId(), previous);
				prioritizedCustomRenderingHooks.add(previous);
			}
			
			throw (PrioritizationFailedException) e.getCause();
		}
	}
	
	public void removeCustomRenderingHook(AbstractCustomRenderingHook hook) {
		
		customRenderingHooks.remove(hook.getId());
		prioritizedCustomRenderingHooks.remove(hook);
	}
}
