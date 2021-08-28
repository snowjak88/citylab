/**
 * 
 */
package org.snowjak.city.service.loadingtasks;

import org.snowjak.city.CityGame;
import org.snowjak.city.map.CityMap;
import org.snowjak.city.screens.LoadingScreen.LoadingTask;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;
import org.snowjak.city.util.RelativePriority;

import com.badlogic.ashley.core.Entity;
import com.github.czyzby.kiwi.log.Logger;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * {@link LoadingTask} that generates new {@link Entity Entities} for every cell
 * of the current {@link CityMap}.
 * 
 * @author snowjak88
 *
 */
public class GameMapEntityCreationTask implements LoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(GameMapEntityCreationTask.class);
	
	private ListenableFuture<?> mapPopulationFuture = null;
	
	private final GameService gameService;
	private final I18NService i18nService;
	private final AtomicDouble progress = new AtomicDouble();
	private final RelativePriority<Class<?>> relativePriority;
	
	public GameMapEntityCreationTask(GameService gameService, I18NService i18nService) {
		
		this.gameService = gameService;
		this.i18nService = i18nService;
		
		this.relativePriority = new RelativePriority<>();
		relativePriority.after(GameEntitySystemInitializationTask.class, GameMapGenerationTask.class);
	}
	
	@Override
	public RelativePriority<Class<?>> getRelativePriority() {
		
		return relativePriority;
	}
	
	@Override
	public String getDescription() {
		
		return i18nService.get("loading-tasks-mapentitycreation");
	}
	
	@Override
	public void initiate() {
		
		if (mapPopulationFuture == null)
			synchronized (this) {
				if (mapPopulationFuture == null)
					initiateTask();
			}
	}
	
	@Override
	public float getProgress() {
		
		final float progressValue = (float) progress.get();
		LOG.info("getProgress() = {0}", progressValue);
		return progressValue;
	}
	
	@Override
	public boolean isComplete() {
		
		if (mapPopulationFuture == null)
			return false;
		
		return mapPopulationFuture.isDone();
	}
	
	private void initiateTask() {
		
		//
		// Check that CityMap exists before trying to kick off the task
		//
		LOG.info("Initiating: verifying map has been generated ...");
		
		final CityMap map = gameService.getState().getMap();
		if (map == null) {
			LOG.error("Cannot initiate: map has not been generated.");
			return;
		}
		
		LOG.info("Starting map-entity-creation task ...");
		mapPopulationFuture = CityGame.EXECUTOR
				.submit(() -> gameService.addCityMapCellEntities(map, (p) -> progress.set(p)));
	}
}
