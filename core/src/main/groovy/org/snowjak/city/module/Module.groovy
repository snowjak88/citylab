package org.snowjak.city.module

import java.util.function.Consumer

import org.snowjak.city.GameData
import org.snowjak.city.map.renderer.hooks.AbstractCellRenderingHook
import org.snowjak.city.map.renderer.hooks.AbstractCustomRenderingHook
import org.snowjak.city.map.renderer.hooks.CellRenderingHook
import org.snowjak.city.map.renderer.hooks.CustomRenderingHook
import org.snowjak.city.map.renderer.hooks.DelegatingCellRenderingHook
import org.snowjak.city.map.renderer.hooks.DelegatingCustomRenderingHook
import org.snowjak.city.resources.ScriptedResource
import org.snowjak.city.service.PreferencesService
import org.snowjak.city.service.PreferencesService.ScopedPreferences
import org.snowjak.city.util.RelativePriority

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
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
	
	private PreferencesService preferencesService
	final GameData data = GameData.get()
	final Map<String,EntitySystem> systems = [:]
	final Set<AbstractCellRenderingHook> cellRenderingHooks = []
	final Set<AbstractCustomRenderingHook> customRenderingHooks = []
	
	Module(PreferencesService preferencesService) {
		super()
		this.preferencesService = preferencesService
	}
	
	/**
	 * Get the {@link ScopedPreferences} instance named "{@code [module-id]}"
	 * from the game's preferences file.
	 * <p>
	 * Ensure that you set [id] before attempting to get this instance.
	 * </p>
	 */
	public ScopedPreferences preferences() {
		preferencesService.get(id)
	}
	
	public RelativePriority cellRenderHook(String id, CellRenderingHook hook) {
		if(isDependencyCheckingMode())
			return new RelativePriority()
		
		def newHook = new DelegatingCellRenderingHook(id, hook)
		cellRenderingHooks << newHook
		newHook.relativePriority
	}
	
	public RelativePriority customRenderHook(id, CustomRenderingHook hook) {
		if(isDependencyCheckingMode())
			return new RelativePriority()
		
		def newHook = new DelegatingCustomRenderingHook(id, hook)
		customRenderingHooks << newHook
		newHook.relativePriority
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
		
		final module = new Module(preferencesService)
		configurer.accept module
		
		script.run()
		
		this.systems.putAll module.systems
		this.cellRenderingHooks.addAll module.cellRenderingHooks
		this.customRenderingHooks.addAll module.customRenderingHooks
		
		module
	}
}
