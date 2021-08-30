/**
* 
*/
package org.snowjak.city.service.loadingtasks;

import org.snowjak.city.CityGame;
import org.snowjak.city.screens.loadingtasks.LoadingTask;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;

import com.github.czyzby.kiwi.log.Logger;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.ListenableFuture;

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
public class GameModulesInitializationTask extends LoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(GameModulesInitializationTask.class);
	
	private ListenableFuture<?> taskFuture = null;
	
	private final GameService gameService;
	private final I18NService i18nService;
	private final AtomicDouble progressHolder = new AtomicDouble();
	
	public GameModulesInitializationTask(GameService gameService, I18NService i18nService) {
		
		this.gameService = gameService;
		this.i18nService = i18nService;
		getRelativePriority().after(GameEntitySystemInitializationTask.class);
	}
	
	@Override
	public String getDescription() {
		
		return i18nService.get("loading-tasks-modules");
	}
	
	@Override
	public void initiate() {
		
		if (taskFuture == null)
			synchronized (this) {
				if (taskFuture == null)
					initializeTask();
			}
	}
	
	@Override
	public float getProgress() {
		
		return (float) progressHolder.get();
	}
	
	@Override
	public boolean isComplete() {
		
		if (taskFuture == null)
			return false;
		
		return taskFuture.isDone();
	}
	
	private void initializeTask() {
		
		LOG.info("Starting module-initialization task ...");
		
		taskFuture = CityGame.EXECUTOR
				.submit(() -> gameService.initializeAllModules((p) -> this.progressHolder.set(p)));
	}
}
