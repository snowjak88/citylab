package org.snowjak.city.screens.loadingtasks;

import org.snowjak.city.util.RelativePriority;
import org.snowjak.city.util.RelativelyPrioritized;

/**
 * Describes a task that needs to be reported by the LoadingScreen.
 * 
 * @author snowjak88
 *
 */
public abstract class LoadingTask implements RelativelyPrioritized<LoadingTask, Class<?>> {
	
	private final RelativePriority<Class<?>> relativePriority = new RelativePriority<>();
	
	/**
	 * @return a piece of descriptive text to display alongside the progress-bar
	 */
	public abstract String getDescription();
	
	/**
	 * Called when this LoadingTask is first initiated.
	 */
	public void initiate() {
		
	};
	
	/**
	 * @return a fraction, in {@code [0,1]}, describing how complete this task is
	 */
	public abstract float getProgress();
	
	/**
	 * @return if this task is complete
	 */
	public abstract boolean isComplete();
	
	@Override
	public Class<?> getRelativePriorityKey() {
		
		return this.getClass();
	}
	
	@Override
	public RelativePriority<Class<?>> getRelativePriority() {
		
		return relativePriority;
	}
}