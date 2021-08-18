/**
 * 
 */
package org.snowjak.city.screens.loadingtasks;

import java.util.concurrent.ExecutionException;

import org.snowjak.city.GameData;
import org.snowjak.city.GameData.GameParameters;
import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.screens.LoadingScreen.LoadingTask;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;
import org.snowjak.city.service.MapGeneratorService;

import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
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
@Component
public class GameMapGenerationTask implements LoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(GameMapGenerationTask.class);
	
	@Inject
	private I18NService i18nService;
	
	@Inject
	private MapGeneratorService mapGeneratorService;
	
	private ListenableFuture<CityMap> mapGenerationFuture = null;
	
	private final AtomicDouble progress = new AtomicDouble();
	
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
				GameData.get().map = mapGenerationFuture.get();
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
		
		final GameData data = GameData.get();
		final GameParameters param = data.parameters;
		if (param.mapWidth <= 0 || param.mapHeight <= 0) {
			LOG.error("Cannot initiate: map dimensions ({0}x{1}) are too low.", param.mapWidth, param.mapHeight);
			return;
		}
		
		LOG.info("Map dimensions = {0}x{1}", param.mapWidth, param.mapHeight);
		
		if (param.selectedMapGenerator == null) {
			if (param.selectedMapGeneratorName == null || param.selectedMapGeneratorName.isEmpty()) {
				LOG.error("Cannot initiate: neither a configured map-generator instance nor name.");
				return;
			}
			
			LOG.info("Map-generator name = {0}", param.selectedMapGeneratorName);
			final MapGenerator mapGenerator = mapGeneratorService.get(param.selectedMapGeneratorName);
			if (mapGenerator == null) {
				LOG.error("Cannot initiate: no map-generator was loaded for the given name (\"{0}\").",
						param.selectedMapGeneratorName);
				return;
			}
			
			param.selectedMapGenerator = mapGenerator;
			
		}
		
		if (param.seed != null && !param.seed.isEmpty())
			data.seed = param.seed;
		
		LOG.info("Seed = {0}", data.seed);
		param.selectedMapGenerator.setSeed(data.seed);
		
		LOG.info("Submitting map-generation task ...");
		mapGenerationFuture = GameData.get().executor.submit(() -> {
			return param.selectedMapGenerator.generate(param.mapWidth, param.mapHeight, (p) -> progress.set(p));
		});
	}
}
