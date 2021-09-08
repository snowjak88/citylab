/**
 * 
 */
package org.snowjak.city.service.loadingtasks;

import org.snowjak.city.map.CityMap;
import org.snowjak.city.screens.loadingtasks.BackgroundLoadingTask;
import org.snowjak.city.screens.loadingtasks.LoadingTask;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;

import com.badlogic.ashley.core.Entity;
import com.github.czyzby.kiwi.log.Logger;

/**
 * {@link LoadingTask} that generates new {@link Entity Entities} for every cell
 * of the current {@link CityMap}.
 * 
 * @author snowjak88
 *
 */
public class GameMapEntityCreationTask extends BackgroundLoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(GameMapEntityCreationTask.class);
	
	private final GameService gameService;
	private final I18NService i18nService;
	
	public GameMapEntityCreationTask(GameService gameService, I18NService i18nService) {
		
		this.gameService = gameService;
		this.i18nService = i18nService;
		
		getRelativePriority().after(GameEntitySystemInitializationTask.class, GameMapGenerationTask.class);
	}
	
	@Override
	public String getDescription() {
		
		return i18nService.get("loading-tasks-mapentitycreation");
	}
	
	@Override
	protected Runnable getTask() {
		
		return () -> {
			gameService.addCityMapLocationEntities(gameService.getState().getMap(), (p) -> setProgress(p));
		};
	}
	
}
