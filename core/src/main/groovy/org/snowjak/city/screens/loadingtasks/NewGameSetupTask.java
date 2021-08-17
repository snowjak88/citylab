/**
 * 
 */
package org.snowjak.city.screens.loadingtasks;

import java.util.LinkedList;

import org.snowjak.city.screens.LoadingScreen.LoadingTask;

import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

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
@Component
public class NewGameSetupTask implements LoadingTask {
	
	private static final Logger LOG = LoggerService.forClass(NewGameSetupTask.class);
	
	@Inject
	private GameMapGenerationTask mapGenerationTask;
	
	@Inject
	private GameEntitySystemInitializationTask entitySystemInitializationTask;
	
	@Inject
	private GameModulesInitializationTask modulesInitializationTask;
	
	private LinkedList<LoadingTask> activeTasks = new LinkedList<>();
	private float progressOffset = 0, taskCount = 0;
	
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
					
					LOG.debug("Initiating: adding modules initialization task ...");
					activeTasks.add(modulesInitializationTask);
					
					LOG.debug("Initiating: initiating first task ...");
					
					activeTasks.peek().initiate();
					
					progressOffset = 0;
					taskCount = 3;
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
		
		return (activeTasks.peek().getProgress() / 3f) + progressOffset;
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
