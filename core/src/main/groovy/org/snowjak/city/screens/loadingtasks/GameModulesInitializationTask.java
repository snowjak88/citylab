/**
 * 
 */
package org.snowjak.city.screens.loadingtasks;

import java.util.Map;

import org.snowjak.city.GameData;
import org.snowjak.city.module.Module;
import org.snowjak.city.screens.LoadingScreen.LoadingTask;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;
import org.snowjak.city.service.ModuleService;

import com.badlogic.ashley.core.EntitySystem;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
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
@Component
public class GameModulesInitializationTask implements LoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(GameModulesInitializationTask.class);
	
	@Inject
	private I18NService i18nService;
	
	@Inject
	private ModuleService moduleService;
	
	private ListenableFuture<?> taskFuture = null;
	
	private final AtomicDouble progress = new AtomicDouble();
	
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
		
		LOG.info("Submitting module-initialization task ...");
		
		taskFuture = GameData.get().executor.submit(() -> {
			progress.set(0);
			
			final GameData data = GameData.get();
			
			final double progressStep = 1d / (double) moduleService.getLoadedNames().size();
			for (String moduleName : moduleService.getLoadedNames()) {
				
				LOG.info("Initializing module \"{0}\"", moduleName);
				
				final Module module = moduleService.get(moduleName);
				
				//
				// Register rendering hooks with the main GameData instance
				//
				
				if (!module.getCellRenderingHooks().isEmpty()) {
					LOG.info("Adding cell rendering hooks ...");
					data.cellRenderingHooks.addAll(module.getCellRenderingHooks());
				}
				
				if (!module.getCustomRenderingHooks().isEmpty()) {
					LOG.info("Adding custom rendering hooks ...");
					data.customRenderingHooks.addAll(module.getCustomRenderingHooks());
				}
				
				//
				// Add this module's systems to the entity engine
				if (!module.getSystems().isEmpty()) {
					LOG.info("Adding entity-processing systems ...");
					for (Map.Entry<String, EntitySystem> systemEntry : module.getSystems().entrySet()) {
						LOG.debug("Adding entity-processing system \"{0}\" ...", systemEntry.getKey());
						data.entityEngine.addSystem(systemEntry.getValue());
					}
				}
				
				LOG.info("Done initializing module \"{0}\".", moduleName);
				progress.addAndGet(progressStep);
			}
		});
	}
}
