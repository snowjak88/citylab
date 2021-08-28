/**
 * 
 */
package org.snowjak.city.service.loadingtasks;

import org.snowjak.city.screens.LoadingScreen.LoadingTask;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.util.RelativePriority;

/**
 * @author snowjak88
 *
 */
public class GameMapRendererSetupTask implements LoadingTask {
	
	private final GameService gameService;
	private final I18NService i18nService;
	private final RelativePriority<Class<?>> relativePriority;
	
	private boolean executed = false;
	
	public GameMapRendererSetupTask(GameService gameService, I18NService i18nService) {
		
		this.gameService = gameService;
		this.i18nService = i18nService;
		
		relativePriority = new RelativePriority<>();
		relativePriority.after(GameMapGenerationTask.class);
	}
	
	@Override
	public RelativePriority<Class<?>> getRelativePriority() {
		
		return relativePriority;
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
