package org.snowjak.city.module

import java.util.function.Consumer

import org.snowjak.city.GameState
import org.snowjak.city.ecs.systems.ListeningSystem
import org.snowjak.city.map.renderer.hooks.AbstractCellRenderingHook
import org.snowjak.city.map.renderer.hooks.AbstractCustomRenderingHook
import org.snowjak.city.map.renderer.hooks.CellRenderingHook
import org.snowjak.city.map.renderer.hooks.CustomRenderingHook
import org.snowjak.city.map.renderer.hooks.DelegatingCellRenderingHook
import org.snowjak.city.map.renderer.hooks.DelegatingCustomRenderingHook
import org.snowjak.city.resources.ScriptedResource
import org.snowjak.city.service.GameService
import org.snowjak.city.service.PreferencesService
import org.snowjak.city.service.PreferencesService.ScopedPreferences
import org.snowjak.city.tools.Tool
import org.snowjak.city.tools.groups.GroupsDefiner
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
public class Module extends ScriptedResource implements GroupsDefiner {
	
	String description
	
	private PreferencesService preferencesService
	private GameService gameService
	
	final GameState state
	final Map<String,EntitySystem> systems = [:]
	
	final Set<AbstractCellRenderingHook> cellRenderingHooks = []
	final Set<AbstractCustomRenderingHook> customRenderingHooks = []
	
	final Map<String,Tool> tools = [:]
	
	Module(GameService gameService, PreferencesService preferencesService) {
		super()
		this.preferencesService = preferencesService
		this.gameService = gameService
		this.state = gameService.state
	}
	
	/**
	 * Get the {@link ScopedPreferences} instance named "{@code [module-id]}"
	 * from the game's preferences file.
	 * <p>
	 * Ensure that you set [id] before attempting to get this instance.
	 * </p>
	 */
	public ScopedPreferences getPreferences() {
		preferencesService.get(id)
	}
	
	/**
	 * Specify a CellRenderingHook to be included in the game's render-loop.
	 * 
	 * @param id Identifies this cell-renderer. Subsequent Modules may overwrite this renderer by using the same ID.
	 * @param hook
	 * @return a {@link RelativePriority prioritizer}
	 */
	public RelativePriority cellRenderHook(String id, CellRenderingHook hook) {
		if(isDependencyCheckingMode())
			return new RelativePriority()
		
		def newHook = new DelegatingCellRenderingHook(id, hook)
		cellRenderingHooks << newHook
		newHook.relativePriority
	}
	
	/**
	 * Specify a CustomRenderHook to be included in the game's render-loop.
	 * 
	 * @param id Identifies this custom-renderer. Subsequent Modules may overwrite this renderer by using the same ID.
	 * @param hook
	 * @return a {@link RelativePriority prioritizer}
	 */
	public RelativePriority customRenderHook(id, CustomRenderingHook hook) {
		if(isDependencyCheckingMode())
			return new RelativePriority()
		
		def newHook = new DelegatingCustomRenderingHook(id, hook)
		customRenderingHooks << newHook
		newHook.relativePriority
	}
	
	/**
	 * Create a new {@link IteratingSystem}.
	 * <p>
	 * {@code implementation} is expected to be of the form:
	 * <pre>
	 * { Entity entity, float deltaTime -> ... }
	 * </pre>
	 * </p>
	 * 
	 * @param id
	 * @param family
	 * @param implementation
	 */
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
	
	/**
	 * Create a new {@link ListeningSystem}.
	 * <p>
	 * Both {@code added} and {@code dropped} are expected to be of the form:
	 * <pre>
	 * { Entity entity, float deltaTime -> ... }
	 * </pre>
	 * </p>
	 *
	 * @param id
	 * @param family
	 * @param implementation
	 */
	public void listeningSystem(String id, Family family, Closure added, Closure dropped) {
		
		if(isDependencyCheckingMode())
			return
		
		def add = added.rehydrate(this, added, added)
		add.resolveStrategy = Closure.DELEGATE_FIRST
		
		def drop = dropped.rehydrate(this, dropped, dropped)
		drop.resolveStrategy = Closure.DELEGATE_FIRST
		
		def system = new ListeningSystem(family) {
					
					@Override
					public void added(Entity entity, float deltaTime) {
						
						add(entity, deltaTime)
					}
					
					@Override
					public void dropped(Entity entity, float deltaTime) {
						
						drop(entity, deltaTime)
					}
				}
		
		systems << ["$id" : system]
	}
	
	/**
	 * Register a new {@link Tool} in this module.
	 * @param id
	 * @param toolSpec
	 */
	public void tool(String id, @DelegatesTo(value=Tool, strategy=Closure.DELEGATE_FIRST) Closure toolSpec) {
		
		final tool = new Tool(id, scriptDirectory, org_snowjak_city_tools_groups_GroupsDefiner__menuGroups, org_snowjak_city_tools_groups_GroupsDefiner__buttonGroups, gameService)
		toolSpec = toolSpec.rehydrate(tool, this, this)
		toolSpec.resolveStrategy = Closure.DELEGATE_FIRST
		toolSpec()
		
		tools << ["$id" : tool]
	}
	
	@Override
	protected ScriptedResource executeInclude(FileHandle includeHandle, Consumer<ScriptedResource> configurer, DelegatingScript script) {
		
		final module = new Module(gameService, preferencesService)
		configurer.accept module
		
		script.run()
		
		this.systems.putAll module.systems
		this.cellRenderingHooks.addAll module.cellRenderingHooks
		this.customRenderingHooks.addAll module.customRenderingHooks
		
		module
	}
}
