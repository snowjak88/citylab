package org.snowjak.city.module

import java.util.function.Consumer

import org.snowjak.city.GameData
import org.snowjak.city.configuration.Configuration
import org.snowjak.city.map.renderer.hooks.AbstractCellRenderingHook
import org.snowjak.city.map.renderer.hooks.AbstractCustomRenderingHook
import org.snowjak.city.map.renderer.hooks.CellRenderingHook
import org.snowjak.city.map.renderer.hooks.CustomRenderingHook
import org.snowjak.city.map.renderer.hooks.DelegatingCellRenderingHook
import org.snowjak.city.map.renderer.hooks.DelegatingCustomRenderingHook
import org.snowjak.city.resources.ScriptedResource

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle

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
public class Module extends ScriptedResource {
	
	String description
	
	final GameData data = GameData.get()
	final Map<String,EntitySystem> systems = [:]
	final Set<AbstractCellRenderingHook> cellRenderingHooks = []
	final Set<AbstractCustomRenderingHook> customRenderingHooks = []
	
	Module() {
		super()
	}
	
	/**
	 * Get the preference named "[module-id].[name]" from the game's preferences file.
	 * If that preference cannot be found, uses {@code defaultValue} as a fallback.
	 * 
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public String preference(String name, String defaultValue = "") {
		Gdx.app.getPreferences(Configuration.PREFERENCES_NAME).getString("$id.$name", defaultValue)
	}
	
	public void cellRenderHook(int priority, CellRenderingHook hook) {
		if(isDependencyCheckingMode())
			return
		
		cellRenderingHooks << new DelegatingCellRenderingHook(priority, hook)
	}
	
	public void renderHook(int priority, CustomRenderingHook hook) {
		if(isDependencyCheckingMode())
			return
		
		customRenderingHooks << new DelegatingCustomRenderingHook(priority, hook)
	}
	
	public void iteratingSystem(String id, Family family, Closure implementation) {
		
		if(isDependencyCheckingMode())
			return
		
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
	
	@Override
	protected ScriptedResource executeInclude(FileHandle includeHandle, Consumer<ScriptedResource> configurer, DelegatingScript script) {
		
		final module = new Module()
		configurer.accept module
		
		script.run()
		
		this.systems.putAll module.systems
		this.cellRenderingHooks.addAll module.cellRenderingHooks
		this.customRenderingHooks.addAll module.customRenderingHooks
		
		module
	}
}
