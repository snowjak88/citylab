/**
 * 
 */
package org.snowjak.city.screens;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.snowjak.city.configuration.Configuration;
import org.snowjak.city.console.Console;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.LoggerService;
import org.snowjak.city.service.SkinService;
import org.snowjak.city.util.RelativePriority;
import org.snowjak.city.util.RelativePriorityList;
import org.snowjak.city.util.RelativePriorityList.PrioritizationFailedException;
import org.snowjak.city.util.RelativelyPrioritized;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.kiwi.log.Logger;

/**
 * Presents a loading screen. Describes what's currently being loaded.
 * 
 * @author snowjak88
 *
 */
@Component
public class LoadingScreen extends AbstractGameScreen {
	
	private static final Logger LOG = LoggerService.forClass(LoadingScreen.class);
	
	private final SkinService skinService;
	
	private LoadingTask loadingTask;
	
	private Label taskDescription;
	private ProgressBar progressBar;
	
	private Runnable onLoadingComplete;
	
	private boolean isInitiated = false;
	
	public LoadingScreen(GameService gameService, Console console, SkinService skinService, Stage stage) {
		
		super(gameService, console, skinService, stage);
		
		this.skinService = skinService;
	}
	
	@Override
	protected Actor getRoot() {
		
		final Skin defaultSkin = skinService.getSkin(Configuration.SKIN_NAME);
		
		taskDescription = new Label("", defaultSkin);
		taskDescription.setAlignment(Align.center);
		
		progressBar = new ProgressBar(0f, 1f, 0.1f, false, defaultSkin);
		progressBar.setAnimateDuration(0.25f);
		
		final Table root = new Table();
		root.setFillParent(true);
		root.center();
		
		root.add(taskDescription);
		root.add(progressBar);
		
		return root;
	}
	
	public void setLoadingTask(LoadingTask loadingTask) {
		
		this.loadingTask = loadingTask;
		isInitiated = false;
	}
	
	public void setLoadingCompleteAction(Runnable onLoadingComplete) {
		
		this.onLoadingComplete = onLoadingComplete;
	}
	
	@Override
	public void beforeStageAct(float delta) {
		
		if (loadingTask == null)
			return;
		
		if (!isInitiated) {
			LOG.info("Initiating current task.");
			loadingTask.initiate();
			isInitiated = true;
		}
		
		progressBar.setValue(loadingTask.getProgress());
		taskDescription.setText(loadingTask.getDescription());
	}
	
	@Override
	public void renderBeforeStage(float delta) {
		
	}
	
	@Override
	public void renderAfterStage(float delta) {
		
		if (loadingTask == null)
			return;
		
		if (!loadingTask.isComplete())
			return;
		
		LOG.info("Task complete -- invoking onLoadingComplete ...");
		loadingTask = null;
		onLoadingComplete.run();
		
	}
	
	/**
	 * Describes a task that needs to be reported by the LoadingScreen.
	 * 
	 * @author snowjak88
	 *
	 */
	public interface LoadingTask extends RelativelyPrioritized<LoadingTask, Class<?>> {
		
		/**
		 * @return a piece of descriptive text to display alongside the progress-bar
		 */
		public String getDescription();
		
		/**
		 * Called when this LoadingTask is first initiated.
		 */
		public default void initiate() {
			
		};
		
		/**
		 * @return a fraction, in {@code [0,1]}, describing how complete this task is
		 */
		public float getProgress();
		
		/**
		 * @return if this task is complete
		 */
		public boolean isComplete();
		
		@Override
		default Class<?> getRelativePriorityKey() {
			
			return this.getClass();
		}
	}
	
	/**
	 * A {@link LoadingTask} that delegates to a prioritized list of
	 * {@link LoadingTask}s.
	 * 
	 * @author snowjak88
	 *
	 */
	public static class CompositeLoadingTask implements LoadingTask {
		
		private final RelativePriorityList<Class<?>, LoadingTask> configuredTasks = new RelativePriorityList<>();
		private final LinkedList<LoadingTask> activeTasks = new LinkedList<>();
		private RelativePriority<Class<?>> relativePriority;
		
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
			
			for (LoadingTask t : tasks)
				configuredTasks.add(t);
			relativePriority = new RelativePriority<>();
		}
		
		@Override
		public RelativePriority<Class<?>> getRelativePriority() {
			
			return relativePriority;
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
}
