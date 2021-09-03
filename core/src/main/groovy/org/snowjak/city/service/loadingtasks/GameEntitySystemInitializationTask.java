/**
 * 
 */
package org.snowjak.city.service.loadingtasks;

import org.snowjak.city.screens.loadingtasks.BackgroundLoadingTask;
import org.snowjak.city.screens.loadingtasks.LoadingTask;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.github.czyzby.kiwi.log.Logger;

/**
 * {@link LoadingTask} that sets up the game's entity-processing {@link Engine},
 * adds all built-in entity-processing {@link EntitySystem systems}, and creates
 * the initial round of {@link Entity Entities} (one for each map-cell).
 * <p>
 * Should be executed <em>after</em> {@link GameMapGenerationTask}.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class GameEntitySystemInitializationTask extends BackgroundLoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(GameEntitySystemInitializationTask.class);
	
	private final GameService gameService;
	private final I18NService i18nService;
	
	public GameEntitySystemInitializationTask(GameService gameService, I18NService i18NService) {
		
		this.gameService = gameService;
		this.i18nService = i18NService;
		
		getRelativePriority().before(GameMapEntityCreationTask.class, GameModulesInitializationTask.class);
	}
	
	@Override
	public String getDescription() {
		
		return i18nService.get("loading-tasks-entitysystem");
	}
	
	@Override
	protected Runnable getTask() {
		
		return () -> {
			
			LOG.info("Adding default entity-processing systems ...");
			
			gameService.initializeBaseEntityEngine((p) -> setProgress(p));
			
			LOG.info("Done!");
		};
	}
	
}
