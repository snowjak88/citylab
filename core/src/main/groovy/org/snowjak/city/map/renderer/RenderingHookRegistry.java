package org.snowjak.city.map.renderer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.snowjak.city.map.renderer.hooks.AbstractCustomRenderingHook;
import org.snowjak.city.map.renderer.hooks.CustomRenderingHook;
import org.snowjak.city.util.PrioritizationFailedException;
import org.snowjak.city.util.RelativePriorityList;

/**
 * Serves as a registry for {@link CustomRenderingHook}s and {@link MapLayer}s.
 * 
 * @author snowjak88
 *
 */
public class RenderingHookRegistry {
	
	private final Map<String, MapLayer> mapLayers = new LinkedHashMap<>();
	private final RelativePriorityList<String, MapLayer> prioritizedMapLayers = new RelativePriorityList<>();
	
	private final Map<String, AbstractCustomRenderingHook> customRenderingHooks = new LinkedHashMap<>();
	private final RelativePriorityList<String, AbstractCustomRenderingHook> prioritizedCustomRenderingHooks = new RelativePriorityList<>();
	
	public List<MapLayer> getPrioritizedMapLayers() {
		
		return prioritizedMapLayers;
	}
	
	public List<AbstractCustomRenderingHook> getPrioritizedCustomRenderingHooks() {
		
		return prioritizedCustomRenderingHooks;
	}
	
	public MapLayer addMapLayer(MapLayer layer) throws PrioritizationFailedException {
		
		final MapLayer previous = mapLayers.put(layer.getId(), layer);
		
		if (previous != null)
			prioritizedMapLayers.remove(previous);
		
		try {
			
			prioritizedMapLayers.add(layer);
			return previous;
			
		} catch (PrioritizationFailedException e) {
			
			if (!(e.getCause() instanceof PrioritizationFailedException))
				throw e;
			
			if (previous == null)
				mapLayers.remove(layer.getId());
			else {
				mapLayers.put(layer.getId(), previous);
				prioritizedMapLayers.add(previous);
			}
			
			throw (PrioritizationFailedException) e.getCause();
		}
	}
	
	public void removeMapLayer(MapLayer layer) {
		
		mapLayers.remove(layer.getId());
		prioritizedMapLayers.remove(layer);
	}
	
	public AbstractCustomRenderingHook addCustomRenderingHook(AbstractCustomRenderingHook hook)
			throws PrioritizationFailedException {
		
		final AbstractCustomRenderingHook previous = customRenderingHooks.put(hook.getId(), hook);
		
		if (previous != null)
			prioritizedCustomRenderingHooks.remove(previous);
		
		try {
			
			prioritizedCustomRenderingHooks.add(hook);
			
			return previous;
			
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
