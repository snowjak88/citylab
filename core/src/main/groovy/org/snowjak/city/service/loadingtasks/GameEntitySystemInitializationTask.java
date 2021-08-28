/**
 * 
 */
package org.snowjak.city.service.loadingtasks;

import org.snowjak.city.CityGame;
import org.snowjak.city.screens.LoadingScreen.LoadingTask;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;
import org.snowjak.city.util.RelativePriority;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.github.czyzby.kiwi.log.Logger;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.ListenableFuture;

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
public class GameEntitySystemInitializationTask implements LoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(GameEntitySystemInitializationTask.class);
	
	private ListenableFuture<?> taskFuture = null;
	
	private final GameService gameService;
	private final I18NService i18nService;
	private final AtomicDouble progress = new AtomicDouble();
	
	private final RelativePriority<Class<?>> relativePriority;
	
	public GameEntitySystemInitializationTask(GameService gameService, I18NService i18NService) {
		
		this.gameService = gameService;
		this.i18nService = i18NService;
		
		this.relativePriority = new RelativePriority<>();
		relativePriority.before(GameMapEntityCreationTask.class, GameModulesInitializationTask.class);
	}
	
	@Override
	public RelativePriority<Class<?>> getRelativePriority() {
		
		return relativePriority;
	}
	
	@Override
	public String getDescription() {
		
		return i18nService.get("loading-tasks-entitysystem");
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
		
		return (float) progress.get();
	}
	
	@Override
	public boolean isComplete() {
		
		if (taskFuture == null)
			return false;
		
		return taskFuture.isDone();
	}
	
	private void initializeTask() {
		
		progress.set(0);
		
		taskFuture = CityGame.EXECUTOR.submit(() -> {
			
			LOG.info("Adding default entity-processing systems ...");
			
			gameService.initializeBaseEntityEngine((p) -> progress.set(p));
			
			LOG.info("Done!");
		});
	}
}
