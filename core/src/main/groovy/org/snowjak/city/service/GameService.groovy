/**
 * 
 */
package org.snowjak.city.service

import java.util.function.BiConsumer
import java.util.function.DoubleConsumer

import org.snowjak.city.ecs.components.IsMapCell
import org.snowjak.city.ecs.systems.impl.IsMapCellManagementSystem
import org.snowjak.city.ecs.systems.impl.RemoveMapCellRearrangedSystem
import org.snowjak.city.map.CityMap
import org.snowjak.city.map.generator.MapGenerator
import org.snowjak.city.map.renderer.MapRenderer
import org.snowjak.city.module.Module
import org.snowjak.city.screens.LoadingScreen.LoadingTask
import org.snowjak.city.service.loadingtasks.GameEntitySystemInitializationTask
import org.snowjak.city.service.loadingtasks.GameMapEntityCreationTask
import org.snowjak.city.service.loadingtasks.GameMapGenerationTask
import org.snowjak.city.service.loadingtasks.GameModulesInitializationTask
import org.snowjak.city.service.loadingtasks.NewGameSetupTask
import org.snowjak.city.util.RelativePriorityList.PrioritizationFailedException

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.core.PooledEngine
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
	private BiConsumer<CityMap,CityMap> cityMapInitializationListener = null
	
	public GameState getState() {
		return state
	}
	
	public LoadingTask getNewGameLoadingTask(NewGameParameters param) {
		return new NewGameSetupTask(
				getMapGenerationLoadingTask(param),
				getEntitySystemInitializationLoadingTask(param),
				getMapCellEntityCreationTask(),
				getModulesInitializationTask())
	}
	
	public LoadingTask getEntitySystemInitializationLoadingTask(NewGameParameters param) {
		
		return new GameEntitySystemInitializationTask(this, i18nService)
	}
	
	public LoadingTask getMapGenerationLoadingTask(NewGameParameters param) {
		return new GameMapGenerationTask(state, param, i18nService)
	}
	
	public LoadingTask getMapCellEntityCreationTask() {
		return new GameMapEntityCreationTask(this, i18nService)
	}
	
	public LoadingTask getModulesInitializationTask() {
		return new GameModulesInitializationTask(this, i18nService, assetService)
	}
	
	/**
	 * Reset the entity-processing {@link Engine} to its base condition, without any Module's systems.
	 */
	public void initializeBaseEntityEngine(DoubleConsumer progressUpdater = {p -> }) {
		
		progressUpdater?.accept 0
		
		if(cityMapInitializationListener) {
			state.cityMapListeners.remove cityMapInitializationListener
			cityMapInitializationListener = null
		}
		
		state.engine.removeAllEntities()
		state.engine.systems.each { state.engine.removeSystem it }
		
		state.engine.addSystem new IsMapCellManagementSystem(state)
		state.engine.addSystem new RemoveMapCellRearrangedSystem()
		
		progressUpdater?.accept 0.5
		
		cityMapInitializationListener = { oldMap, newMap ->
			removeCityMapCellEntities oldMap
			addCityMapCellEntities newMap
		}
		state.cityMapListeners << cityMapInitializationListener
		
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
	 * Ensure the given {@link Module} is properly initialized into the current GameState.
	 * @param module
	 */
	public void initializeModule(Module module) {
		
		LOG.info "Initializing module \"{0}\"", module.id
		
		//
		// Register rendering hooks with the main GameData instance
		//
		
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
		
		//
		// Add this module's systems to the entity engine
		if (!module.systems.isEmpty()) {
			LOG.info "Adding entity-processing systems ..."
			for (def systemEntry : module.systems) {
				LOG.debug "Adding entity-processing system \"{0}\" ...", systemEntry.key
				state.engine.addSystem systemEntry.value
			}
		}
		LOG.info "Finished initialized module \"{0}\".", module.id
	}
	
	/**
	 * Ensure the given {@link Module} is completely removed from the current GameState.
	 * @param module
	 */
	public void uninitializeModule(Module module) {
		
		LOG.info "Uninitializing module \"{0}\" ...", module.id
		
		//
		// Remove this module's rendering-hooks
		if(!module.cellRenderingHooks.isEmpty()) {
			LOG.info "Removing cell-rendering hooks ..."
			for(def hook : module.cellRenderingHooks)
				state.renderer.removeCellRenderingHook hook
		}
		
		if(!module.customRenderingHooks.isEmpty()) {
			LOG.info "Removing custom-rendering hooks ..."
			for(def hook : module.customRenderingHooks)
				state.renderer.removeCustomRenderingHook hook
		}
		
		//
		// Remove this module's entity-processing systems
		if(!module.systems.isEmpty()) {
			LOG.info "Removing entity-processing systems ..."
			for(def systemEntry : module.systems)
				state.engine.removeSystem(systemEntry.value)
		}
		
		LOG.info "Finished uninitializing module \"{0}\".", module.id
	}
	
	public static class GameState {
		
		/**
		 * If you want to be notified when {@link #map} is assigned, add a {@link BiConsumer} here.
		 * Your BiConsumer will be called with the previous and new values of {@link #map} whenever that field
		 * is updated.
		 */
		Set<BiConsumer<CityMap, CityMap>> cityMapListeners = Collections.synchronizedSet(new LinkedHashSet<>())
		
		/**
		 * Seed to be used for random-number generation.
		 */
		String seed = Long.toString(System.currentTimeMillis())
		
		/**
		 * Active {@link CityMap} (may be {@code null})
		 */
		CityMap map
		
		/**
		 * Entity-processing {@link Engine}
		 */
		final Engine engine = new PooledEngine(64, 512, 8, 64)
		
		/**
		 * The main world renderer
		 */
		final MapRenderer renderer = new MapRenderer()
		
		//
		// Intercept property-setters so as to hook in our listeners.
		void setProperty(String name, Object value) {
			def previousValue = this.@"$name"
			this.@"$name" = value
			if(name == 'map')
				cityMapListeners.each { it.accept previousValue, value }
		}
	}
	
	public static class NewGameParameters {
		int mapWidth = 64, mapHeight = 64
		MapGenerator generator = null
		String seed = null
	}
}
