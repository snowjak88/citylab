/**
 * 
 */
package org.snowjak.city.service.loadingtasks;

import org.snowjak.city.GameState;
import org.snowjak.city.map.CityMap;
import org.snowjak.city.screens.loadingtasks.BackgroundLoadingTask;
import org.snowjak.city.screens.loadingtasks.LoadingTask;
import org.snowjak.city.service.GameService.NewGameParameters;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;

import com.github.czyzby.kiwi.log.Logger;

/**
 * {@link LoadingTask} that generates a new {@link CityMap} for
 * {@link GameData#map}, using the configured {@link GameData#parameters
 * parameters}. Stores the completed CityMap instance in GameData (overwriting
 * whatever was there before).
 * 
 * @author snowjak88
 *
 */
public class GameMapGenerationTask extends BackgroundLoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(GameMapGenerationTask.class);
	
	private final GameState state;
	private final NewGameParameters param;
	private final I18NService i18nService;
	
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
	protected Runnable getTask() {
		
		return () -> {
			LOG.info("Initiating: checking parameters ...");
			
			if (param.getMapWidth() <= 0 || param.getMapHeight() <= 0)
				throw new MapGenerationException("Cannot initiate: map dimensions (" + param.getMapWidth() + "x"
						+ param.getMapHeight() + ") are too low.");
			
			LOG.info("Map dimensions = {0}x{1}", param.getMapWidth(), param.getMapHeight());
			
			if (param.getGenerator() == null)
				throw new MapGenerationException("Cannot initiate: no specified map-generator.");
			
			if (param.getSeed() != null && !param.getSeed().isEmpty())
				state.setSeed(param.getSeed());
			
			LOG.info("Seed = {0}", state.getSeed());
			param.getGenerator().setSeed(state.getSeed());
			
			LOG.info("Generating map ...");
			final CityMap map = param.getGenerator().generate(param.getMapWidth(), param.getMapHeight(),
					(p) -> setProgress(p));
			
			state.setMap(map);
		};
	}
	
	public static class MapGenerationException extends RuntimeException {
		
		private static final long serialVersionUID = 8515110574124277072L;
		
		public MapGenerationException() {
			
			super();
		}
		
		public MapGenerationException(String message, Throwable cause) {
			
			super(message, cause);
		}
		
		public MapGenerationException(String message) {
			
			super(message);
		}
		
		public MapGenerationException(Throwable cause) {
			
			super(cause);
		}
		
	}
}
