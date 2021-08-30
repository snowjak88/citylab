/**
 * 
 */
package org.snowjak.city.screens;

import org.snowjak.city.configuration.Configuration;
import org.snowjak.city.console.Console;
import org.snowjak.city.screens.loadingtasks.LoadingTask;
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
}
