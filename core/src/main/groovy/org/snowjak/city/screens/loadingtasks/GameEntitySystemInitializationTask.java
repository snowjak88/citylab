/**
 * 
 */
package org.snowjak.city.screens.loadingtasks;

import org.snowjak.city.GameData;
import org.snowjak.city.ecs.components.IsMapCell;
import org.snowjak.city.ecs.systems.impl.IsMapCellManagementSystem;
import org.snowjak.city.ecs.systems.impl.RemoveMapCellRearrangedSystem;
import org.snowjak.city.screens.LoadingScreen.LoadingTask;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
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
@Component
public class GameEntitySystemInitializationTask implements LoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(GameEntitySystemInitializationTask.class);
	
	@Inject
	private I18NService i18nService;
	
	private ListenableFuture<?> taskFuture = null;
	
	private final AtomicDouble progress = new AtomicDouble();
	
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
		
		final GameData data = GameData.get();
		if (data.map == null)
			return;
		
		if (data.engine == null)
			data.engine = new PooledEngine(64, 1024, 4, 64);
		
		taskFuture = GameData.get().executor.submit(() -> {
			
			LOG.info("Adding default entity-processing systems ...");
			
			data.engine.addSystem(new IsMapCellManagementSystem());
			data.engine.addSystem(new RemoveMapCellRearrangedSystem());
			
			//
			// Add Entities for every map-cell ...
			LOG.info("Adding Entity for every map-cell ...");
			
			final double progressStep = 1.0 / ((double) data.map.getWidth() * (double) data.map.getHeight());
			
			for (int x = 0; x < data.map.getWidth(); x++)
				for (int y = 0; y < data.map.getHeight(); y++) {
					final IsMapCell mapCell = data.engine.createComponent(IsMapCell.class);
					mapCell.setCellX(x);
					mapCell.setCellY(y);
					
					final Entity entity = data.engine.createEntity();
					data.engine.addEntity(entity);
					entity.add(mapCell);
					
					progress.addAndGet(progressStep);
				}
			
			LOG.info("Done!");
		});
	}
}
