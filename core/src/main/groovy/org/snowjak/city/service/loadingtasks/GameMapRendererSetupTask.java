/**
 * 
 */
package org.snowjak.city.service.loadingtasks;

import org.snowjak.city.screens.loadingtasks.LoadingTask;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;

/**
 * @author snowjak88
 *
 */
public class GameMapRendererSetupTask extends LoadingTask {
	
	private final GameService gameService;
	private final I18NService i18nService;
	
	private boolean executed = false;
	
	public GameMapRendererSetupTask(GameService gameService, I18NService i18nService) {
		
		this.gameService = gameService;
		this.i18nService = i18nService;
		
		getRelativePriority().after(GameMapGenerationTask.class);
	}
	
	@Override
	public String getDescription() {
		
		return i18nService.get("loading-tasks-renderer");
	}
	
	@Override
	public float getProgress() {
		
		return (executed) ? 1 : 0;
	}
	
	@Override
	public boolean isComplete() {
		
		return executed;
	}
	
	@Override
	public void initiate() {
		
		gameService.intializeRenderer();
		executed = true;
	}
	
}
