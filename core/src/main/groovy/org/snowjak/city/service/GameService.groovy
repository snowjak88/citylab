/**
 * 
 */
package org.snowjak.city.service

import java.util.function.DoubleConsumer

import org.snowjak.city.ecs.components.IsMapCell
import org.snowjak.city.ecs.systems.impl.IsMapCellManagementSystem
import org.snowjak.city.ecs.systems.impl.RemoveMapCellRearrangedSystem
import org.snowjak.city.map.CityMap
import org.snowjak.city.map.generator.MapGenerator
import org.snowjak.city.map.renderer.MapRenderer
import org.snowjak.city.module.Module
import org.snowjak.city.screens.GameScreen.GameCameraControl
import org.snowjak.city.screens.LoadingScreen.CompositeLoadingTask
import org.snowjak.city.screens.LoadingScreen.LoadingTask
import org.snowjak.city.service.loadingtasks.GameEntitySystemInitializationTask
import org.snowjak.city.service.loadingtasks.GameMapEntityCreationTask
import org.snowjak.city.service.loadingtasks.GameMapGenerationTask
import org.snowjak.city.service.loadingtasks.GameMapRendererSetupTask
import org.snowjak.city.service.loadingtasks.GameModulesInitializationTask
import org.snowjak.city.util.RelativePriorityList.PrioritizationFailedException

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.github.czyzby.autumn.annotation.Component
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
	private GameAssetService assetService
	
	@Inject
	private I18NService i18nService
	
	private final GameState state = new GameState()
	
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
				new GameModulesInitializationTask(this, i18nService),
				new GameMapRendererSetupTask(this, i18nService))
	}
	
	/**
	 * Reset the entity-processing {@link Engine} to its base condition, without any Module's systems.
	 */
	public void initializeBaseEntityEngine(DoubleConsumer progressUpdater = {p -> }) {
		
		progressUpdater?.accept 0
		
		state.engine.removeAllEntities()
		state.engine.systems.each { state.engine.removeSystem it }
		
		state.engine.addSystem new IsMapCellManagementSystem(state)
		state.engine.addSystem new RemoveMapCellRearrangedSystem()
		
		progressUpdater?.accept 1.0
	}
	
	/**
	 * Remove all {@link IsMapCell}-bearing Entities from the entity-processing {@link Engine}.
	 * @param map
	 */
	public void removeCityMapCellEntities(CityMap map, DoubleConsumer progressReporter = { p -> }) {
		
		def entities = state.engine.getEntitiesFor(Family.all(IsMapCell).get())
		
		final progressStep = 1.0 / (double) entities.size()
		def progress = 0.0
		
		for(def entity : entities) {
			def component = (IsMapCell) entity.remove(IsMapCell)
			if(component && map)
				map.setEntity component.cellX, component.cellY, null
			
			state.engine.removeEntity entity
			
			progress += progressStep
			progressReporter?.accept progress
		}
	}
	
	/**
	 * Create new {@link IsMapCell}-bearing Entities for every cell in the
	 * given Map, add those Entities to the entity-processing {@link Engine},
	 * and associate those Entities with the Map.
	 * @param map
	 */
	public void addCityMapCellEntities(CityMap map, DoubleConsumer progressReporter = { p -> }) {
		if(map) {
			
			final progressStep = 1.0 / ((double) map.width * (double) map.height)
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
					
					progress += progressStep
					progressReporter?.accept progress
				}
		}
	}
	
	/**
	 * Invokes {@link #initializeModule(Module) initializeModule()} for all loaded {@link Modules}s.
	 */
	public void initializeAllModules(DoubleConsumer progressReporter = { p -> }) {
		LOG.info "Initializing all modules ..."
		
		final modules = assetService.getAllByType(Module)
		final progressStep = 1d / (double) modules.size()
		def progress = 0d
		
		progressReporter?.accept 0
		
		for(Module m : modules) {
			initializeModule m
			
			progress += progressStep
			progressReporter?.accept progress
		}
		
		progressReporter?.accept 1
		
		LOG.info "Finished fnitializing all modules."
	}
	
	/**
	 * Invokes {@link #uninitializeModule(Module) unitializeModule()} for all loaded {@link Module}s.
	 */
	public void uninitializeAllModules(DoubleConsumer progressReporter = { p -> }) {
		LOG.info "Uninitializing all modules ..."
		
		final modules = assetService.getAllByType(Module)
		final progressStep = 1d / (double) modules.size()
		def progress = 0d
		
		progressReporter?.accept 0
		
		for(Module m : assetService.getAllByType(Module)) {
			uninitializeModule m
			
			progress += progressStep
			progressReporter?.accept progress
		}
		
		progressReporter?.accept 1
		
		LOG.info "Finished uninitializing all modules."
	}
	
	/**
	 * Ensure the given {@link Module} is properly initialized into the current GameState.
	 * @param module
	 */
	public void initializeModule(Module module, DoubleConsumer progressReporter = { p -> }) {
		
		LOG.info "Initializing module \"{0}\"", module.id
		
		//
		// Register rendering hooks with the main GameData instance
		//
		
		progressReporter?.accept 0
		
		if (!module.cellRenderingHooks.isEmpty()) {
			LOG.info "Adding cell rendering hooks ..."
			for (def hook : module.cellRenderingHooks)
				try {
					state.renderer.addCellRenderingHook hook
				} catch (PrioritizationFailedException e) {
					LOG.error "Cannot initialize cell-rendering hook [{0}] for module [{1}] -- too many conflicting priorities!",
							hook.id, module.id
				}
		}
		
		progressReporter?.accept 0.334
		
		if (!module.customRenderingHooks.isEmpty()) {
			LOG.info "Adding custom rendering hooks ..."
			for (def hook : module.customRenderingHooks)
				try {
					state.renderer.addCustomRenderingHook hook
				} catch (PrioritizationFailedException e) {
					LOG.error "Cannot initialize custom-rendering hook [{0}] for module [{1}] -- too many conflicting priorities!",
							hook.id, module.id
				}
		}
		
		progressReporter?.accept 0.667
		
		//
		// Add this module's systems to the entity engine
		if (!module.systems.isEmpty()) {
			LOG.info "Adding entity-processing systems ..."
			for (def systemEntry : module.systems) {
				LOG.debug "Adding entity-processing system \"{0}\" ...", systemEntry.key
				state.engine.addSystem systemEntry.value
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
		
		//
		// Remove this module's rendering-hooks
		if(!module.cellRenderingHooks.isEmpty()) {
			LOG.info "Removing cell-rendering hooks ..."
			for(def hook : module.cellRenderingHooks)
				state.renderer.removeCellRenderingHook hook
		}
		
		progressReporter?.accept 0.334
		
		if(!module.customRenderingHooks.isEmpty()) {
			LOG.info "Removing custom-rendering hooks ..."
			for(def hook : module.customRenderingHooks)
				state.renderer.removeCustomRenderingHook hook
		}
		
		progressReporter?.accept 0.667
		
		//
		// Remove this module's entity-processing systems
		if(!module.systems.isEmpty()) {
			LOG.info "Removing entity-processing systems ..."
			for(def systemEntry : module.systems)
				state.engine.removeSystem(systemEntry.value)
		}
		
		progressReporter?.accept 1
		
		LOG.info "Finished uninitializing module \"{0}\".", module.id
	}
	
	/**
	 * (Re-)initialize the configured renderer.
	 */
	public void intializeRenderer() {
		state.renderer.map = state.map
	}
	
	/**
	 * Exit now. Do not save. Do not pass Go. Do not collect $200.
	 */
	public void exitNow() {
		Gdx.app.exit()
	}
	
	public static class GameState {
		
		/**
		 * Seed to be used for random-number generation.
		 */
		String seed = Long.toString(System.currentTimeMillis())
		
		/**
		 * Active {@link CityMap} (may be {@code null})
		 */
		CityMap map
		
		/**
		 * Active camera-controller (may be {@code null})
		 */
		GameCameraControl camera
		
		/**
		 * Entity-processing {@link Engine}
		 */
		final Engine engine = new PooledEngine(64, 512, 8, 64)
		
		/**
		 * The main world renderer
		 */
		final MapRenderer renderer = new MapRenderer()
	}
	
	public static class NewGameParameters {
		int mapWidth = 64, mapHeight = 64
		MapGenerator generator = null
		String seed = null
	}
}
