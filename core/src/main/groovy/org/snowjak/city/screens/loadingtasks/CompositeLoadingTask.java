package org.snowjak.city.screens.loadingtasks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.snowjak.city.util.PrioritizationFailedException;
import org.snowjak.city.util.RelativePriorityList;

/**
 * A {@link LoadingTask} that delegates to a prioritized list of
 * {@link LoadingTask}s.
 * 
 * @author snowjak88
 *
 */
public class CompositeLoadingTask extends LoadingTask {
	
	private final RelativePriorityList<Class<?>, LoadingTask> configuredTasks = new RelativePriorityList<>();
	private final LinkedList<LoadingTask> activeTasks = new LinkedList<>();
	
	private float progressOffset = 0, taskCount = 0;
	
	/**
	 * Construct a new CompositeLoadingTask, composed of the given
	 * {@link LoadingTask}s.
	 * 
	 * @param tasks
	 * @throws PrioritizationFailedException
	 *             if the tasks cannot be successfully prioritized -- e.g., if there
	 *             is a circular dependency somewhere
	 */
	public CompositeLoadingTask(LoadingTask... tasks) throws PrioritizationFailedException {
		
		this(new HashSet<>(Arrays.asList(tasks)));
	}
	
	/**
	 * Construct a new CompositeLoadingTask, composed of the given
	 * {@link LoadingTask}s.
	 * 
	 * @param tasks
	 * @throws PrioritizationFailedException
	 *             if the tasks cannot be successfully prioritized -- e.g., if there
	 *             is a circular dependency somewhere
	 */
	public CompositeLoadingTask(Set<LoadingTask> tasks) throws PrioritizationFailedException {
		
		super();
		
		for (LoadingTask t : tasks)
			configuredTasks.add(t);
	}
	
	@Override
	public void initiate() {
		
		if (activeTasks.isEmpty())
			synchronized (this) {
				if (activeTasks.isEmpty()) {
					
					for (LoadingTask t : configuredTasks)
						activeTasks.addLast(t);
					
					progressOffset = 0;
					taskCount = activeTasks.size();
					
					if (!activeTasks.isEmpty())
						activeTasks.peek().initiate();
					
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
			
			progressOffset += 1f / taskCount;
			activeTasks.removeFirst();
			
			if (activeTasks.isEmpty())
				return true;
			
			activeTasks.peek().initiate();
			
		}
		
		return false;
	}
	
}