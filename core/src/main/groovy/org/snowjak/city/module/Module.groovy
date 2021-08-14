package org.snowjak.city.module

import org.snowjak.city.GameData
import org.snowjak.city.configuration.Configuration
import org.snowjak.city.map.renderer.hooks.AbstractCellRenderingHook
import org.snowjak.city.map.renderer.hooks.AbstractCustomRenderingHook
import org.snowjak.city.map.renderer.hooks.CellRenderingHook
import org.snowjak.city.map.renderer.hooks.CustomRenderingHook
import org.snowjak.city.map.renderer.hooks.DelegatingCellRenderingHook
import org.snowjak.city.map.renderer.hooks.DelegatingCustomRenderingHook
import org.snowjak.city.map.tiles.TileSet
import org.snowjak.city.service.ResourceService

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx

/**
 * A Module provides game functionality.
 * <p>
 * Technically, a Module defines one or more {@link EntitySystem}s, along with
 * their {@link Component}s. These EntitySystems implement specific aspects of
 * game-functionality.
 * </p>
 * <p>
 * A Module may also define UI elements (buttons, windows, dialogs, ...) and may
 * register input-receivers directly.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class Module {
	
	String id, description
	
	ResourceService<TileSet,TileSet> tileSetService
	
	final GameData data = GameData.get()
	final Map<String,EntitySystem> systems = [:]
	final Set<AbstractCellRenderingHook> cellRenderingHooks = []
	final Set<AbstractCustomRenderingHook> customRenderingHooks = []
	final Binding binding = new Binding()
	
	def propertyMissing = { name ->
		//
		// Attempt to locate any missing properties in our Binding
		binding[name]
	}
	
	public String preference(String name, String defaultValue = "") {
		Gdx.app.getPreferences(Configuration.PREFERENCES_NAME).getString("$id.$name", defaultValue)
	}
	
	public void cellRenderHook(int priority, CellRenderingHook hook) {
		cellRenderingHooks << new DelegatingCellRenderingHook(priority, hook)
	}
	
	public void renderHook(int priority, CustomRenderingHook hook) {
		customRenderingHooks << new DelegatingCustomRenderingHook(priority, hook)
	}
	
	public void iteratingSystem(String id, Family family, Closure implementation) {
		
		def imp = implementation.rehydrate(this, implementation, implementation)
		imp.resolveStrategy = Closure.DELEGATE_FIRST
		
		def system = new IteratingSystem(family) {
					
					@Override
					protected void processEntity(Entity entity, float deltaTime) {
						
						imp(entity, deltaTime)
					}
				}
		
		systems << ["$id" : system]
	}
}
