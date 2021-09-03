package org.snowjak.city.screens.loadingtasks;

import java.util.concurrent.atomic.AtomicReference;

import org.snowjak.city.CityGame;
import org.snowjak.city.util.RelativePriority;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * A LoadingTask with plumbing to enable your task (written as a Runnable) to
 * execute in the background.
 * 
 * @author snowjak88
 *
 */
public abstract class BackgroundLoadingTask extends LoadingTask {
	
	private final RelativePriority<Class<?>> relativePriority = new RelativePriority<>();
	
	private final AtomicDouble progressValue = new AtomicDouble();
	private final AtomicReference<Throwable> taskException = new AtomicReference<>();
	private ListenableFuture<?> taskFuture;
	
	/**
	 * @return a piece of descriptive text to display alongside the progress-bar
	 */
	public abstract String getDescription();
	
	/**
	 * Called when this LoadingTask is first initiated.
	 */
	@Override
	final public void initiate() {
		
		taskException.set(null);
		taskFuture = CityGame.EXECUTOR.submit(wrapTask(getTask()));
	};
	
	/**
	 * @return a fraction, in {@code [0,1]}, describing how complete this task is
	 */
	@Override
	final public double getProgress() {
		
		return progressValue.get();
	}
	
	/**
	 * Report your task's progress as a fraction, in {@code [0,1]}.
	 * 
	 * @param progress
	 */
	final protected void setProgress(double progress) {
		
		progressValue.set(progress);
	}
	
	/**
	 * Return your task as a {@link Runnable}.
	 * 
	 * @return
	 */
	protected abstract Runnable getTask();
	
	final private Runnable wrapTask(Runnable task) {
		
		return () -> {
			try {
				task.run();
			} catch (Throwable t) {
				taskException.set(t);
			}
		};
	}
	
	/**
	 * @return if this task is complete
	 */
	@Override
	final public boolean isComplete() {
		
		return (taskFuture == null) || (taskFuture.isCancelled()) || (taskFuture.isDone());
	}
	
	/**
	 * @return {@code true} if this task has failed with an exception
	 */
	@Override
	final public boolean isFailed() {
		
		return (isComplete()) && (taskException.get() != null);
	}
	
	/**
	 * @return the Throwable thrown off by this task, if any
	 */
	@Override
	final public Throwable getException() {
		
		return taskException.get();
	}
	
	@Override
	public Class<?> getRelativePriorityKey() {
		
		return this.getClass();
	}
	
	@Override
	public RelativePriority<Class<?>> getRelativePriority() {
		
		return relativePriority;
	}
}