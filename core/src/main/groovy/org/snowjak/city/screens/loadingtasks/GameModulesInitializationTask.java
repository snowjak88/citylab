/**
 * 
 */
package org.snowjak.city.screens.loadingtasks;

import java.util.Collection;
import java.util.Map;

import org.snowjak.city.GameData;
import org.snowjak.city.map.renderer.hooks.AbstractCellRenderingHook;
import org.snowjak.city.map.renderer.hooks.AbstractCustomRenderingHook;
import org.snowjak.city.module.Module;
import org.snowjak.city.screens.LoadingScreen.LoadingTask;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;

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
	private GameAssetService assetService;
	
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
			
			final Collection<Module> allModules = assetService.getAllByType(Module.class);
			final double progressStep = 1d / (double) allModules.size();
			for (Module module : allModules) {
				
				LOG.info("Initializing module \"{0}\"", module.getId());
				
				//
				// Register rendering hooks with the main GameData instance
				//
				
				if (!module.getCellRenderingHooks().isEmpty()) {
					LOG.info("Adding cell rendering hooks ...");
					module.getCellRenderingHooks().forEach(h -> data.cellRenderingHooks.put(h.getId(), h));
				}
				
				if (!module.getCustomRenderingHooks().isEmpty()) {
					LOG.info("Adding custom rendering hooks ...");
					module.getCustomRenderingHooks().forEach(h -> data.customRenderingHooks.put(h.getId(), h));
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
				
				LOG.info("Done initializing module \"{0}\".", module.getId());
				progress.addAndGet(progressStep);
			}
			
			LOG.info("Prioritizing cell-rendering hooks ...");
			data.prioritizedCellRenderingHooks.clear();
			
			for (AbstractCellRenderingHook hook : data.cellRenderingHooks.values())
				try {
					data.prioritizedCellRenderingHooks.add(hook);
				} catch (RuntimeException e) {
					LOG.error(e, "Cannot configure cell-rendering-hook {0} -- too many conflicting priorities!",
							hook.getId());
				}
			
			LOG.info("Cell-rendering hooks prioritized:");
			int i = 1;
			for (AbstractCellRenderingHook hook : data.prioritizedCellRenderingHooks)
				LOG.info("[{0}]: {1}", i++, hook.getId());
			
			LOG.info("Prioritizing custom-rendering hooks ...");
			data.prioritizedCustomRenderingHooks.clear();
			
			for (AbstractCustomRenderingHook hook : data.customRenderingHooks.values())
				try {
					data.prioritizedCustomRenderingHooks.add(hook);
				} catch (RuntimeException e) {
					LOG.error(e, "Cannot configure custom-rendering-hook {0} -- too many conflicting priorities!",
							hook.getId());
				}
			
			LOG.info("Custom-rendering hooks prioritized:");
			i = 1;
			for (AbstractCustomRenderingHook hook : data.prioritizedCustomRenderingHooks)
				LOG.info("[{0}]: {1}", i++, hook.getId());
		});
	}
}
