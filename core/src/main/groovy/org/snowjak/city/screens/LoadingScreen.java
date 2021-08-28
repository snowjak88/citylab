/**
 * 
 */
package org.snowjak.city.screens;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.snowjak.city.configuration.Configuration;
import org.snowjak.city.console.Console;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.LoggerService;
import org.snowjak.city.service.SkinService;

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
	
	private final LinkedList<LoadingTask> loadingTasks = new LinkedList<>();
	private float loadingTaskCount;
	private float loadingTaskProgressOffset = 0f;
	
	private Label taskDescription;
	private ProgressBar progressBar;
	
	private Runnable onLoadingComplete;
	
	private boolean isInitiated = false, invokedComplete = false;
	
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
	
	public void setLoadingTasks(LoadingTask... loadingTasks) {
		
		setLoadingTasks(Arrays.asList(loadingTasks));
	}
	
	public void setLoadingTasks(List<LoadingTask> loadingTasks) {
		
		this.loadingTasks.addAll(loadingTasks);
		loadingTaskCount = this.loadingTasks.size();
		loadingTaskProgressOffset = 0;
		isInitiated = false;
	}
	
	public void setLoadingCompleteAction(Runnable onLoadingComplete) {
		
		this.onLoadingComplete = onLoadingComplete;
		invokedComplete = false;
	}
	
	@Override
	public void beforeStageAct(float delta) {
		
		if (loadingTasks.isEmpty())
			return;
		
		if (!isInitiated) {
			LOG.info("Initiating current task.");
			loadingTasks.peek().initiate();
			isInitiated = true;
		}
		
		progressBar.setValue(loadingTaskProgressOffset + loadingTasks.peek().getProgress() / loadingTaskCount);
		taskDescription.setText(loadingTasks.peek().getDescription());
	}
	
	@Override
	public void renderBeforeStage(float delta) {
		
	}
	
	@Override
	public void renderAfterStage(float delta) {
		
		if (!loadingTasks.isEmpty())
			if (loadingTasks.peek().isComplete()) {
				LOG.info("Current task is complete -- removing from the list of remaining tasks.");
				loadingTaskProgressOffset += 1f / loadingTaskCount;
				loadingTasks.pop();
				isInitiated = false;
			}
		
		if (loadingTasks.isEmpty() && !invokedComplete) {
			LOG.info("All tasks complete -- invoking onLoadingComplete ...");
			onLoadingComplete.run();
			invokedComplete = true;
		}
	}
	
	/**
	 * Describes a task that needs to be reported by the LoadingScreen.
	 * 
	 * @author snowjak88
	 *
	 */
	public interface LoadingTask {
		
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
	}
}
