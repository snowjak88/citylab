/**
* 
*/
package org.snowjak.city.service.loadingtasks;

import org.snowjak.city.screens.loadingtasks.BackgroundLoadingTask;
import org.snowjak.city.screens.loadingtasks.LoadingTask;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;

import com.github.czyzby.kiwi.log.Logger;

/**
 * {@link LoadingTask} that initializes all loaded {@link Module}s into
 * {@link GameData}.
 * <p>
 * Should be executed <em>after</em> {@link GameEntitySystemInitializationTask}.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class GameModulesInitializationTask extends BackgroundLoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(GameModulesInitializationTask.class);
	
	private final GameService gameService;
	private final I18NService i18nService;
	
	public GameModulesInitializationTask(GameService gameService, I18NService i18nService) {
		
		this.gameService = gameService;
		this.i18nService = i18nService;
		getRelativePriority().after(GameMapGenerationTask.class, GameEntitySystemInitializationTask.class,
				GameMapEntityCreationTask.class);
	}
	
	@Override
	public String getDescription() {
		
		return i18nService.get("loading-tasks-modules");
	}
	
	@Override
	protected Runnable getTask() {
		
		return () -> gameService.initializeAllModules((p) -> setProgress(p));
	}
}
