/**
 * 
 */
package org.snowjak.city.service.loadingtasks;

import java.util.Collection;

import org.snowjak.city.CityGame;
import org.snowjak.city.module.Module;
import org.snowjak.city.screens.LoadingScreen.LoadingTask;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;
import org.snowjak.city.util.RelativePriority;

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
public class GameModulesInitializationTask implements LoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(GameModulesInitializationTask.class);
	
	private ListenableFuture<?> taskFuture = null;
	
	private final GameService gameService;
	private final I18NService i18nService;
	private final GameAssetService assetService;
	private final AtomicDouble progress = new AtomicDouble();
	private final RelativePriority<Class<?>> relativePriority;
	
	public GameModulesInitializationTask(GameService gameService, I18NService i18nService,
			GameAssetService assetService) {
		
		this.gameService = gameService;
		this.i18nService = i18nService;
		this.assetService = assetService;
		this.relativePriority = new RelativePriority<>();
		relativePriority.after(GameEntitySystemInitializationTask.class);
	}
	
	@Override
	public RelativePriority<Class<?>> getRelativePriority() {
		
		return relativePriority;
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
		
		return (float) progress.get();
	}
	
	@Override
	public boolean isComplete() {
		
		if (taskFuture == null)
			return false;
		
		return taskFuture.isDone();
	}
	
	private void initializeTask() {
		
		LOG.info("Starting module-initialization task ...");
		
		taskFuture = CityGame.EXECUTOR.submit(() -> {
			progress.set(0);
			
			final Collection<Module> allModules = assetService.getAllByType(Module.class);
			final double progressStep = 1d / (double) allModules.size();
			for (Module module : allModules) {
				
				gameService.initializeModule(module);
				
				LOG.info("Done initializing module \"{0}\".", module.getId());
				progress.addAndGet(progressStep);
			}
		});
	}
}
