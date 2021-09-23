package org.snowjak.city.service.loadingtasks;

import org.snowjak.city.screens.loadingtasks.BackgroundLoadingTask;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;

import com.github.czyzby.kiwi.log.Logger;

public class GameModulesUninitializationTask extends BackgroundLoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(GameModulesUninitializationTask.class);
	
	private final GameService gameService;
	private final I18NService i18nService;
	
	public GameModulesUninitializationTask(GameService gameService, I18NService i18nService) {
		
		this.gameService = gameService;
		this.i18nService = i18nService;
		getRelativePriority().before(GameMapEntityDestructionTask.class);
	}
	
	@Override
	public String getDescription() {
		
		return i18nService.get("loading-tasks-uninit-modules");
	}
	
	@Override
	protected Runnable getTask() {
		
		return () -> gameService.uninitializeAllModules((p) -> setProgress(p));
	}
	
}
