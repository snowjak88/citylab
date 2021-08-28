/**
 * 
 */
package org.snowjak.city.service.loadingtasks;

import java.util.LinkedList;

import org.snowjak.city.screens.LoadingScreen.LoadingTask;
import org.snowjak.city.service.LoggerService;

import com.github.czyzby.kiwi.log.Logger;

/**
 * Convenience {@link LoadingTask}. A composite of:
 * <ol>
 * <li>{@link GameMapGenerationTask}</li>
 * <li>{@link GameEntitySystemInitializationTask}</li>
 * <li>{@link GameModulesInitializationTask}</li>
 * </ol>
 * 
 * @author snowjak88
 *
 */
public class NewGameSetupTask implements LoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(NewGameSetupTask.class);
	
	private final GameMapGenerationTask mapGenerationTask;
	private final GameEntitySystemInitializationTask entitySystemInitializationTask;
	private final GameMapEntityCreationTask mapEntityCreationTask;
	private final GameModulesInitializationTask modulesInitializationTask;
	
	private LinkedList<LoadingTask> activeTasks = new LinkedList<>();
	private float progressOffset = 0, taskCount = 0;
	
	public NewGameSetupTask(GameMapGenerationTask mapGenerationTask,
			GameEntitySystemInitializationTask entitySystemInitializationTask,
			GameMapEntityCreationTask mapEntityCreationTask, GameModulesInitializationTask modulesInitializationTask) {
		
		this.mapGenerationTask = mapGenerationTask;
		this.entitySystemInitializationTask = entitySystemInitializationTask;
		this.mapEntityCreationTask = mapEntityCreationTask;
		this.modulesInitializationTask = modulesInitializationTask;
	}
	
	@Override
	public void initiate() {
		
		if (activeTasks.isEmpty())
			synchronized (this) {
				if (activeTasks.isEmpty()) {
					
					LOG.info("Initiating: adding sub-tasks ...");
					
					LOG.debug("Initiating: adding map-generation task ...");
					activeTasks.add(mapGenerationTask);
					
					LOG.debug("Initiating: adding entity-system initialization task ...");
					activeTasks.add(entitySystemInitializationTask);
					
					LOG.debug("Initiating: adding map-cell-entity initialization task ...");
					activeTasks.add(mapEntityCreationTask);
					
					LOG.debug("Initiating: adding modules initialization task ...");
					activeTasks.add(modulesInitializationTask);
					
					LOG.debug("Initiating: initiating first task ...");
					
					activeTasks.peek().initiate();
					
					progressOffset = 0;
					taskCount = activeTasks.size();
				}
			}
	}
	
	@Override
	public String getDescription() {
		
		if (activeTasks.isEmpty())
			return "";
		
		return activeTasks.peek().getDescription();
	}
	
	@Override
	public float getProgress() {
		
		if (activeTasks.isEmpty())
			return 1;
		
		return (activeTasks.peek().getProgress() / taskCount) + progressOffset;
	}
	
	@Override
	public boolean isComplete() {
		
		if (activeTasks.isEmpty())
			return true;
		
		if (activeTasks.peek().isComplete()) {
			
			LOG.info("NewGameSetupTask: active task is complete, getting next task ...");
			
			progressOffset += 1f / taskCount;
			activeTasks.pop();
			
			if (activeTasks.isEmpty()) {
				LOG.info("NewGameSetupTask: no more tasks -- all done!");
				return true;
			}
			
			activeTasks.peek().initiate();
		}
		
		return false;
	}
	
}
