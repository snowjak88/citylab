package org.snowjak.city.module

import org.snowjak.city.ecs.GatheringEntityListener
import org.snowjak.city.map.renderer.hooks.AbstractRenderingHook
import org.snowjak.city.module.ui.VisualParameter
import org.snowjak.city.tools.Tool
import org.snowjak.city.tools.ToolGroup

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.maps.MapLayer

/**
 * Facade to a {@link Module} instance that only gives you access to the Module's collections
 * ({@link Module#systems}, etc.) and its explicitly-"provided" objects.
 * 
 * @author snowjak88
 *
 */
class ModulePublicFace {
	private final Module module
	
	ModulePublicFace(Module module) {
		this.module = module
		
		ModulePublicFace.metaClass.methodMissing = { name, args ->
			getProperty(name).call(*args)
		}
	}
	
	def propertyMissing(name) {
		module.providedObjects[name]
	}
	
	public String getId() {
		module.id
	}
	
	public String getTitle() {
		module.title
	}
	
	public String getDescription() {
		module.description
	}
	
	public boolean isEnabled() {
		module.enabled
	}
	public void setEnabled(boolean enabled) {
		module.enabled = enabled
	}
	
	public Set<Runnable> getOnActivationActions() {
		module.onActivationActions
	}
	
	public Set<Runnable> getOnDeactivationActions() {
		module.onDeactivationActions
	}
	
	public Map<String,Module> getModules() {
		module.modules
	}
	
	public Map<String,EntitySystem> getSystems() {
		module.systems
	}
	
	public Set<GatheringEntityListener> getEntityListeners() {
		module.entityListeners
	}
	
	public Set<MapLayer> getMapLayers() {
		module.mapLayers
	}
	
	public Map<String,AbstractRenderingHook> getRenderingHooks() {
		module.renderingHooks
	}
	
	public Map<String,ToolGroup> getToolGroups() {
		module.toolGroups
	}
	
	public Map<String,Tool> getTools() {
		module.tools
	}
	
	public Set<VisualParameter> getVisualParameters() {
		module.visualParameters
	}
}
