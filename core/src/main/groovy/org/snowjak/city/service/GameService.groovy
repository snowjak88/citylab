/**
 * 
 */
package org.snowjak.city.service

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.util.function.DoubleConsumer

import org.snowjak.city.CityGame
import org.snowjak.city.GameState
import org.snowjak.city.configuration.InitPriority
import org.snowjak.city.ecs.components.HasMapCellTiles
import org.snowjak.city.ecs.components.IsMapCell
import org.snowjak.city.ecs.components.IsMapVertex
import org.snowjak.city.ecs.systems.impl.IsMapCellManagementSystem
import org.snowjak.city.ecs.systems.impl.IsMapVertexManagementSystem
import org.snowjak.city.ecs.systems.impl.MapCellBlockerRemovingSystem
import org.snowjak.city.ecs.systems.impl.RemoveMapCellRearrangedSystem
import org.snowjak.city.ecs.systems.impl.RemoveMapVertexRearrangedSystem
import org.snowjak.city.ecs.systems.impl.UnselectAllEventSystem
import org.snowjak.city.map.CityMap
import org.snowjak.city.map.generator.MapGenerator
import org.snowjak.city.map.renderer.hooks.DelegatingRenderingHook
import org.snowjak.city.module.Module
import org.snowjak.city.module.ModuleExceptionRegistry.FailureDomain
import org.snowjak.city.screens.loadingtasks.CompositeLoadingTask
import org.snowjak.city.screens.loadingtasks.LoadingTask
import org.snowjak.city.service.loadingtasks.GameEntitySystemInitializationTask
import org.snowjak.city.service.loadingtasks.GameMapEntityCreationTask
import org.snowjak.city.service.loadingtasks.GameMapGenerationTask
import org.snowjak.city.service.loadingtasks.GameModulesInitializationTask
import org.snowjak.city.util.PrioritizationFailedException

import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.czyzby.autumn.annotation.Component
import com.github.czyzby.autumn.annotation.Initiate
import com.github.czyzby.autumn.annotation.Inject
import com.github.czyzby.kiwi.log.Logger

/**
 * Provides game-state management.
 * 
 * @author snowjak88
 *
 */
@Component
class GameService {
	
	private static final Logger LOG = LoggerService.forClass(GameService)
	
	@Inject
	private SkinService skinService
	
	@Inject
	private Stage stage
	
	private final GameAssetService assetService
	private final I18NService i18nService
	private final GameState state
	
	public GameService(GameAssetService assetService, I18NService i18nService) {
		this.assetService = assetService
		this.i18nService = i18nService
		this.state = new GameState(assetService, i18nService)
	}
	
	@Initiate(priority=InitPriority.HIGHEST_PRIORITY)
	public void init() {
		//
		// Whenever we get around to setting these state-objects,
		// ensure they get completely configured
		state.addPropertyChangeListener 'toolbar', { PropertyChangeEvent e ->
			if(e.newValue)
				initializeToolbar()
		} as PropertyChangeListener
	}
	
	public GameState getState() {
		return state
	}
	
	/**
	 * Construct a {@link LoadingTask} that will reset the {@link GameState} to a new game
	 * (described by the given {@link NewGameParameters}).
	 * @param param
	 * @return
	 */
	public LoadingTask getNewGameLoadingTask(NewGameParameters param) {
		return new CompositeLoadingTask(
				new GameMapGenerationTask(state, param, i18nService),
				new GameEntitySystemInitializationTask(this, i18nService),
				new GameMapEntityCreationTask(this, i18nService),
				new GameModulesInitializationTask(this, i18nService) )
	}
	
	/**
	 * Reset the entity-processing {@link Engine} to its base condition, without any Module's systems.
	 */
	public void initializeBaseEntityEngine(DoubleConsumer progressUpdater = { p -> }) {
		
		progressUpdater?.accept 0
		
		state.engine.removeAllEntities()
		state.engine.systems.each {
			state.engine.removeSystem it
		}
		
		state.engine.addSystem new IsMapCellManagementSystem(state)
		state.engine.addSystem new IsMapVertexManagementSystem(state)
		state.engine.addSystem new RemoveMapCellRearrangedSystem()
		state.engine.addSystem new RemoveMapVertexRearrangedSystem()
		state.engine.addSystem new MapCellBlockerRemovingSystem()
		state.engine.addSystem new UnselectAllEventSystem()
		
		progressUpdater?.accept 1.0
	}
	
	/**
	 * Remove all {@link IsMapCell}-bearing Entities from the entity-processing {@link Engine}.
	 * @param map
	 */
	public void removeCityMapLocationEntities(CityMap map, DoubleConsumer progressReporter = { p ->
			}) {
		
		def entities = state.engine.getEntitiesFor(Family.one(IsMapCell, IsMapVertex).get())
		
		final progressStep = 1.0 / (double) entities.size()
		def progress = 0.0
		
		for(def entity : entities) {
			final mapCell = (IsMapCell) entity.remove(IsMapCell)
			if(mapCell && map)
				map.setEntity( (int) mapCell.cellX, (int) mapCell.cellY, null )
			
			entity.remove HasMapLayers
			
			final mapVertex = (IsMapVertex) entity.remove(IsMapVertex)
			if(mapVertex && map)
				map.setVertexEntity( (int) mapVertex.vertexX, (int) mapVertex.vertexY, null )
			
			state.engine.removeEntity entity
			
			progress += progressStep
			progressReporter?.accept progress
		}
	}
	
	/**
	 * Create new {@link IsMapCell} and {@link IsMapVertex}-bearing Entities for every cell/vertex in the
	 * given Map, add those Entities to the entity-processing {@link Engine},
	 * and associate those Entities with the Map.
	 * @param map
	 */
	public void addCityMapLocationEntities(CityMap map, DoubleConsumer progressReporter = { p -> }) {
		if(map) {
			
			final progressStep = 2.0 / ((double) map.width * (double) map.height)
			def progress = 0
			
			for(def x=0; x<map.width; x++)
				for(def y=0; y<map.height; y++) {
					def isMapCell = state.engine.createComponent(IsMapCell)
					isMapCell.cellX = x
					isMapCell.cellY = y
					
					def entity = state.engine.createEntity()
					entity.add isMapCell
					map.setEntity x, y, entity
					state.engine.addEntity entity
					
					entity.add state.engine.createComponent(HasMapCellTiles)
					
					progress += progressStep
					progressReporter?.accept progress
				}
			
			for(def x=0; x<map.width+1; x++)
				for(def y=0; y<map.height+1; y++) {
					def isMapVertex = state.engine.createComponent(IsMapVertex)
					isMapVertex.vertexX = x
					isMapVertex.vertexY = y
					
					def entity = state.engine.createEntity()
					entity.add isMapVertex
					map.setVertexEntity x, y, entity
					state.engine.addEntity entity
					
					progress += progressStep
					progressReporter?.accept progress
				}
		}
	}
	
	/**
	 * Reloads all loaded {@link Module}s from their script-files.
	 * Blocks until all Modules are uninitialized, reloaded, and re-initialized.
	 *
	 * @param progressReporter
	 */
	public void reloadAllModules(DoubleConsumer progressReporter = { p ->
			}) {
		LOG.info "Reloading all modules ..."
		
		progressReporter?.accept 0
		
		uninitializeAllModules { p ->
			progressReporter?.accept p * 0.2
		}
		
		final modules = assetService.getAllByType(Module)
		
		unloadAllModules { p ->
			progressReporter.accept p*0.2 + 0.2
		}
		
		loadAllModules { p ->
			progressReporter.accept p*0.2 + 0.4
		}
		
		while(!assetService.update())
			progressReporter?.accept assetService.progress * 0.2 + 0.6
		
		initializeAllModules { p ->
			progressReporter?.accept p * 0.2 + 0.8
		}
		
		LOG.info "Finished reloading all modules."
	}
	
	/**
	 * Load module-scripts. Skips over all module-scripts that are already loaded.
	 * @param progressReporter
	 */
	public void loadAllModules(DoubleConsumer progressReporter = { p ->
			}) {
		
		LOG.info "Scanning for module-scripts ..."
		
		final resolver = GameAssetService.FILE_HANDLE_RESOLVER
		final moduleFiles = scanForFiles(resolver.resolve(CityGame.EXTERNAL_ROOT_MODULES), ".module.groovy", true)
		
		final progressStep = 1d / (double) moduleFiles.size()
		def progress = 0d
		
		for(def f : moduleFiles) {
			
			if(assetService.isLoaded(f.path(), Module)) {
				LOG.info "Module-script [{0}] is already loaded, skipping ...", f.path()
				continue
			}
			
			LOG.info "Loading module-script [{0}] ...", f.path()
			assetService.load(f.path(), Module.class)
			
			progress += progressStep
			progressReporter?.accept progress
		}
		
		LOG.info "Finished scanning for module-scripts."
	}
	
	/**
	 * Un-load all loaded module-scripts.
	 * @param progressReporter
	 */
	public void unloadAllModules(DoubleConsumer progressReporter = { p ->
			}) {
		
		LOG.info "Unloading all modules ..."
		
		final modules = assetService.getAllByType(Module)
		
		final progressStep = 1d / (double) modules.size()
		def progress = 0d
		
		for(def m : modules) {
			assetService.unload m.id, Module
			
			progress += progressStep
			progressReporter?.accept progress
		}
		
		LOG.info "Finished unloading all modules."
	}
	
	/**
	 * Invokes {@link #initializeModule(Module) initializeModule()} for all loaded {@link Modules}s.
	 */
	public void initializeAllModules(DoubleConsumer progressReporter = { p ->
			}) {
		LOG.info "Initializing all modules ..."
		
		final modules = assetService.getAllByType(Module)
		
		final progressStep = 1d / (double) modules.size()
		def progress = 0d
		
		progressReporter?.accept 0
		
		for(Module m : modules) {
			if(m.enabled)
				initializeModule m, { p ->
					progressReporter?.accept p / progressStep + progress
				}
			
			progress += progressStep
		}
		
		progressReporter?.accept 1
		
		LOG.info "Finished initializing all modules."
	}
	
	/**
	 * Invokes {@link #uninitializeModule(Module) unitializeModule()} for all loaded {@link Module}s.
	 */
	public void uninitializeAllModules(DoubleConsumer progressReporter = { p ->
			}) {
		LOG.info "Uninitializing all modules ..."
		
		final progressStep = 1d / (double) state.modules.size()
		def progress = 0d
		
		progressReporter?.accept 0
		
		for(Module m : assetService.getAllByType(Module)) {
			if(m.enabled)
				uninitializeModule m, { p ->
					progressReporter p / progressStep + progress
				}
			
			progress += progressStep
		}
		
		progressReporter?.accept 1
		
		LOG.info "Finished uninitializing all modules."
	}
	
	/**
	 * Ensure the given {@link Module} is properly initialized into the current GameState.
	 * @param module
	 */
	public void initializeModule(Module module, DoubleConsumer progressReporter = { p ->
			}) {
		
		LOG.info "Initializing module \"{0}\" ...", module.id
		
		final overriddenModule = state.modules.put(module.id, module)
		if(overriddenModule)
			throw new RuntimeException("Looks like we've already initialized a module with the same ID -- ${overriddenModule.scriptFile.path()}. The GameAssetService should never have let this happen.")
		
		//
		// Register rendering hooks with the main GameData instance
		//
		
		progressReporter?.accept 0
		
		initializeModuleRenderingHooks module, { p -> progressReporter?.accept p * 0.5f }
		
		progressReporter?.accept 0.5
		
		//
		// Add this module's systems to the entity engine
		if (!module.systems.isEmpty()) {
			LOG.info "Adding entity-processing systems ..."
			for (def systemEntry : module.systems) {
				LOG.debug "Adding entity-processing system \"{0}\" ...", systemEntry.key
				state.engine.addSystem systemEntry.value
			}
		}
		
		//
		// Add this module's entity-listeners to the entity engine
		if (!module.entityListeners.isEmpty()) {
			LOG.info "Adding entity-listeners ..."
			module.entityListeners.each { l -> state.engine.addEntityListener l.family, l }
		}
		
		progressReporter?.accept 0.75
		
		if(!module.tools.isEmpty()) {
			LOG.info "Adding tools ..."
			for(def toolEntry : module.tools) {
				LOG.info "Adding tool \"{0}\" ...", toolEntry.key
				
				final overriddenTool = state.tools[toolEntry.key]
				if(overriddenTool)
					LOG.info "Overrode Tool from \"${overriddenTool.module.id}\" [${overriddenTool.module.scriptFile.path()}]"
				
				for(def hotkey : toolEntry.value.hotkeys) {
					LOG.info "Registering tool hotkey \"${hotkey.key}\" = \"${hotkey.value.toString()}\" ..."
					final tool = toolEntry.value
					state.hotkeys.register hotkey.value, { -> tool.toggle() }
				}
				
				state.tools["$toolEntry.key"] = toolEntry.value
			}
			
			initializeToolbar()
		}
		
		if(!module.windows.isEmpty()) {
			LOG.info "Adding windows ..."
			for(def windowEntry : module.windows) {
				if(!windowEntry.value)
					continue
				
				LOG.info "Adding window \"{0}\" ...", windowEntry.key
				
				final overriddenWindow = state.windows[windowEntry.key]
				if(overriddenWindow)
					LOG.info "Overrode window from \"${overriddenWindow.module.id}\" [${overriddenWindow.module.scriptFile.path()}]"
				
				state.windows["$windowEntry.key"] = windowEntry.value
			}
		}
		
		progressReporter?.accept 1
		
		LOG.info "Finished initialized module \"{0}\".", module.id
	}
	
	/**
	 * Ensure the given {@link Module} is completely removed from the current GameState.
	 * @param module
	 */
	public void uninitializeModule(Module module, DoubleConsumer progressReporter = { p -> }) {
		
		LOG.info "Uninitializing module \"{0}\" ...", module.id
		
		progressReporter?.accept 0
		
		if(!module.onDeactivationActions.isEmpty()) {
			LOG.info "Executing on-deactivate actions ..."
			
			module.onDeactivationActions.each {
				try {
					it.run()
				} catch(Throwable t) {
					state.moduleExceptionRegistry.reportFailure module, FailureDomain.OTHER, t
				}
			}		}
		
		//
		// Remove this module's rendering-hooks
		
		uninitializeModuleRenderingHooks module, { p -> progressReporter?.accept p * 0.5f }
		
		progressReporter?.accept 0.5
		
		//
		// Remove this module's entity-listeners
		if(!module.entityListeners.isEmpty()) {
			LOG.info "Removing entity-listeners ..."
			module.entityListeners.each { l -> state.engine.removeEntityListener l }
		}
		
		//
		// Remove this module's entity-processing systems
		if(!module.systems.isEmpty()) {
			LOG.info "Removing entity-processing systems ..."
			for(def systemEntry : module.systems)
				state.engine.removeSystem(systemEntry.value)
		}
		
		if(!module.tools.isEmpty()) {
			LOG.info "Removing tools ..."
			for(def tool : module.tools) {
				
				for(def hotkey : tool.value.hotkeys)
					state.hotkeys.unregister hotkey.value
				
				state.tools.remove tool.key
			}
			
			initializeToolbar()
		}
		
		if(!module.windows.isEmpty()) {
			LOG.info "Removing windows ..."
			for(def window : module.windows)
				state.windows.remove window.key
		}
		
		state.modules.remove module.id
		
		progressReporter?.accept 1
		
		LOG.info "Finished uninitializing module \"{0}\".", module.id
	}
	
	public void initializeModuleRenderingHooks(Module module, DoubleConsumer progressReporter = {p -> }) {
		
		progressReporter?.accept 0
		
		state.mapModes.putAll module.mapModes
		
		if (!module.renderingHooks.isEmpty()) {
			LOG.info "Adding rendering hooks ..."
			final progressStep = 1f / (float)module.renderingHooks.size()
			def progress = 0
			
			for (def hook : module.renderingHooks)
				try {
					final previousHook = state.renderingHookRegistry.addRenderingHook hook.value
					
					if(previousHook)
						if(previousHook instanceof DelegatingRenderingHook)
							LOG.info "\"$hook.value.id\" overrides render-hook from \"${(previousHook as DelegatingRenderingHook).module.id}\" [${(previousHook as DelegatingRenderingHook).module.scriptFile.path()}]"
				} catch (PrioritizationFailedException e) {
					LOG.error "Cannot initialize rendering hook [{0}] for module [{1}] -- too many conflicting priorities!",
							hook.value.id, module.id
				} finally {
					progress += progressStep
					progressReporter?.accept progress
				}
		}
	}
	
	public void uninitializeModuleRenderingHooks(Module module, DoubleConsumer progressReporter = {p -> }) {
		
		if(!module.renderingHooks.isEmpty()) {
			LOG.info "Removing rendering hooks ..."
			
			final progressStep = 1f / (float)module.renderingHooks.size()
			def progress = 0
			
			for(def hook : module.renderingHooks) {
				state.renderingHookRegistry.removeRenderingHook hook.value
				
				progress += progressStep
				progressReporter?.accept progress
			}
		}
		
	}
	
	/**
	 * (Re-)Initialize the toolbar, if it's available.
	 * (It might not be available yet, in which case this method does nothing.)
	 */
	public void initializeToolbar() {
		
		state.activeTool?.deactivate()
		state.toolbar?.removeAllTools()
		state.toolbar?.addTools state.tools.values()
	}
	
	private Collection<FileHandle> scanForFiles(FileHandle directory, String desiredExtension,
			boolean includeSubdirectories) {
		
		final LinkedList<FileHandle> result = new LinkedList<>()
		
		for (FileHandle child : directory.list())
			if (child.isDirectory()) {
				if (includeSubdirectories)
					result.addAll(scanForFiles(child, desiredExtension, includeSubdirectories))
			} else if (child.name().endsWith(desiredExtension))
				result.add(child)
		
		return result
	}
	
	/**
	 * Exit now. Do not save. Do not pass Go. Do not collect $200.
	 */
	public void exitNow() {
		Gdx.app.exit()
	}
	
	public static class NewGameParameters {
		int mapWidth = 64, mapHeight = 64
		MapGenerator generator = null
		String seed = null
	}
}
