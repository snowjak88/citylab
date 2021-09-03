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
	public abstract double getProgress();
	
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
	
	/**
	 * @return the Throwable thrown off by this task, if any (by default, this
	 *         method returns {@code null})
	 */
	public Throwable getException() {
		
		return null;
	}
	
	/**
	 * @return {@code true} if this task has failed with an exception (by default,
	 *         this method returns {@code false})
	 */
	public boolean isFailed() {
		
		return false;
	}
}