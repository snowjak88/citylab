/**
 * 
 */
package org.snowjak.city.service.loadingtasks;

import java.util.concurrent.ExecutionException;

import org.snowjak.city.CityGame;
import org.snowjak.city.GameState;
import org.snowjak.city.map.CityMap;
import org.snowjak.city.screens.loadingtasks.LoadingTask;
import org.snowjak.city.service.GameService.NewGameParameters;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;

import com.github.czyzby.kiwi.log.Logger;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * {@link LoadingTask} that generates a new {@link CityMap} for
 * {@link GameData#map}, using the configured {@link GameData#parameters
 * parameters}. Stores the completed CityMap instance in GameData (overwriting
 * whatever was there before).
 * 
 * @author snowjak88
 *
 */
public class GameMapGenerationTask extends LoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(GameMapGenerationTask.class);
	
	private ListenableFuture<CityMap> mapGenerationFuture = null;
	
	private final GameState state;
	private final NewGameParameters param;
	private final I18NService i18nService;
	private final AtomicDouble progress = new AtomicDouble();
	
	public GameMapGenerationTask(GameState state, NewGameParameters param, I18NService i18nService) {
		
		this.state = state;
		this.param = param;
		this.i18nService = i18nService;
		getRelativePriority().before(GameMapEntityCreationTask.class);
	}
	
	@Override
	public String getDescription() {
		
		return i18nService.get("loading-tasks-mapgeneration");
	}
	
	@Override
	public void initiate() {
		
		if (mapGenerationFuture == null)
			synchronized (this) {
				if (mapGenerationFuture == null)
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
		
		if (mapGenerationFuture == null)
			return false;
		
		if (mapGenerationFuture.isDone()) {
			try {
				state.setMap(mapGenerationFuture.get());
				return true;
			} catch (InterruptedException | ExecutionException e) {
				return true;
			}
		}
		
		return false;
	}
	
	private void initiateTask() {
		
		//
		// Check parameters before trying to kick off the task
		//
		LOG.info("Initiating: checking parameters ...");
		
		if (param.getMapWidth() <= 0 || param.getMapHeight() <= 0) {
			LOG.error("Cannot initiate: map dimensions ({0}x{1}) are too low.", param.getMapWidth(),
					param.getMapHeight());
			return;
		}
		
		LOG.info("Map dimensions = {0}x{1}", param.getMapWidth(), param.getMapHeight());
		
		if (param.getGenerator() == null) {
			LOG.error("Cannot initiate: no specified map-generator.");
			return;
		}
		
		if (param.getSeed() != null && !param.getSeed().isEmpty())
			state.setSeed(param.getSeed());
		
		LOG.info("Seed = {0}", state.getSeed());
		param.getGenerator().setSeed(state.getSeed());
		
		LOG.info("Starting map-generation task ...");
		mapGenerationFuture = CityGame.EXECUTOR.submit(() -> {
			return param.getGenerator().generate(param.getMapWidth(), param.getMapHeight(), (p) -> progress.set(p));
		});
	}
}
