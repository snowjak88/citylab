package org.snowjak.city.map.renderer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.snowjak.city.map.renderer.hooks.AbstractRenderingHook;
import org.snowjak.city.map.renderer.hooks.RenderingHook;
import org.snowjak.city.util.PrioritizationFailedException;
import org.snowjak.city.util.RelativePriorityList;

/**
 * Serves as a registry for {@link RenderingHook}s and {@link MapLayer}s.
 * 
 * @author snowjak88
 *
 */
public class RenderingHookRegistry {
	
	private final Map<String, MapLayer> mapLayers = new LinkedHashMap<>();
	private final RelativePriorityList<String, MapLayer> prioritizedMapLayers = new RelativePriorityList<>();
	
	private final Map<String, AbstractRenderingHook> renderingHooks = new LinkedHashMap<>();
	private final RelativePriorityList<String, AbstractRenderingHook> prioritizedRenderingHooks = new RelativePriorityList<>();
	
	public List<MapLayer> getPrioritizedMapLayers() {
		
		return prioritizedMapLayers;
	}
	
	public List<AbstractRenderingHook> getPrioritizedRenderingHooks() {
		
		return prioritizedRenderingHooks;
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
	
	public AbstractRenderingHook addRenderingHook(AbstractRenderingHook hook)
			throws PrioritizationFailedException {
		
		final AbstractRenderingHook previous = renderingHooks.put(hook.getId(), hook);
		
		if (previous != null)
			prioritizedRenderingHooks.remove(previous);
		
		try {
			
			prioritizedRenderingHooks.add(hook);
			
			return previous;
			
		} catch (RuntimeException e) {
			
			if (!(e.getCause() instanceof PrioritizationFailedException))
				throw e;
			
			if (previous == null)
				renderingHooks.remove(hook.getId());
			else {
				renderingHooks.put(hook.getId(), previous);
				prioritizedRenderingHooks.add(previous);
			}
			
			throw (PrioritizationFailedException) e.getCause();
		}
	}
	
	public void removeRenderingHook(AbstractRenderingHook hook) {
		
		renderingHooks.remove(hook.getId());
		prioritizedRenderingHooks.remove(hook);
	}
}
