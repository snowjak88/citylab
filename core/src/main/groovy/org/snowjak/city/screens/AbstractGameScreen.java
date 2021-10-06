/**
 * 
 */
package org.snowjak.city.screens;

import java.util.LinkedList;

import org.snowjak.city.console.Console;
import org.snowjak.city.module.ui.ModuleExceptionReportingWindow;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.SkinService;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.github.czyzby.kiwi.util.gdx.GdxUtilities;

/**
 * @author snowjak88
 *
 */
public abstract class AbstractGameScreen extends ScreenAdapter {
	
	public static final float SCREEN_FADE_TIME = 0.2f;
	
	private final LinkedList<Runnable> postShowActions = new LinkedList<>();
	
	private final GameService gameService;
	private final Console console;
	private final I18NService i18nService;
	private final SkinService skinService;
	private final GameAssetService assetService;
	private final Stage stage;
	
	private ModuleExceptionReportingWindow exceptionReportingWindow;
	
	private Actor root;
	
	private Label fpsLabel;
	
	private Skin skin;
	
	private boolean clearScreen = true;
	private Color backgroundColor, skinBackgroundColor;
	
	private Game game;
	
	public AbstractGameScreen(GameService gameService, Console console, I18NService i18nService,
			SkinService skinService, GameAssetService assetService, Stage stage) {
		
		this.gameService = gameService;
		this.console = console;
		this.stage = stage;
		this.i18nService = i18nService;
		this.skinService = skinService;
		this.assetService = assetService;
	}
	
	public void setGame(Game game) {
		
		this.game = game;
	}
	
	public void changeScreen(Screen screen) {
		
		stage.getRoot().addAction(Actions.sequence(Actions.fadeOut(SCREEN_FADE_TIME),
				Actions.run(() -> this.game.setScreen(screen)), Actions.fadeIn(SCREEN_FADE_TIME)));
	}
	
	/**
	 * Add the following "post-show" action. When this screen is shown, this action
	 * will be executed. After execution, this action is discarded.
	 * 
	 * @param action
	 */
	public void addPostShowAction(Runnable action) {
		
		postShowActions.add(action);
	}
	
	/**
	 * Will this screen automatically clear itself with every frame? (default =
	 * true)
	 * 
	 * @return
	 */
	public boolean isClearScreen() {
		
		return clearScreen;
	}
	
	/**
	 * Set whether this screen will automatically clear itself with every frame.
	 * (default = true)
	 * 
	 * @param clearScreen
	 */
	public void setClearScreen(boolean clearScreen) {
		
		this.clearScreen = clearScreen;
	}
	
	/**
	 * Clear the screen using the configured background color (if any)
	 */
	public void clearScreen() {
		
		final Color clearColor;
		if (backgroundColor != null)
			clearColor = backgroundColor;
		else if (skinBackgroundColor != null)
			clearColor = skinBackgroundColor;
		else
			clearColor = null;
		
		if (clearColor != null)
			GdxUtilities.clearScreen(clearColor.r, clearColor.g, clearColor.b);
		else
			GdxUtilities.clearScreen();
	}
	
	public Color getBackgroundColor() {
		
		return backgroundColor;
	}
	
	public void setBackgroundColor(Color backgroundColor) {
		
		this.backgroundColor = backgroundColor;
	}
	
	/**
	 * Performs the following steps:
	 * <ol>
	 * <li>Sets up an {@link InputMultiplexer} between {@link #getInputProcessor()
	 * your screen's InputProcessor} and the Stage</li>
	 * <li>Calls {@link #getRoot()} to get this screen's root {@link Actor}</li>
	 * <li>Schedules that root to fade in over {@link #SCREEN_FADE_TIME} seconds
	 * </li>
	 * </ol>
	 */
	@Override
	public void show() {
		
		final InputProcessor implementationInputProcessor = getInputProcessor();
		if (implementationInputProcessor == null)
			Gdx.input.setInputProcessor(new InputMultiplexer(console.getInputProcessor(), stage));
		else
			Gdx.input.setInputProcessor(
					new InputMultiplexer(console.getInputProcessor(), stage, implementationInputProcessor));
		
		skin = skinService.getCurrent();
		if (skin.has("background", Color.class))
			skinBackgroundColor = skin.get("background", Color.class);
		else
			skinBackgroundColor = null;
		
		fpsLabel = new Label("", skinService.getCurrent());
		fpsLabel.setPosition(15, 15);
		stage.addActor(fpsLabel);
		
		root = getRoot();
		if (root != null) {
			stage.getRoot().addActor(root);
			root.getColor().a = 0;
			
			root.addAction(Actions.fadeIn(SCREEN_FADE_TIME));
		}
		
		this.exceptionReportingWindow = new ModuleExceptionReportingWindow(i18nService, skinService);
		this.exceptionReportingWindow
				.addDismissAction(() -> gameService.getState().getModuleExceptionRegistry().nextFailure());
		stage.addActor(exceptionReportingWindow);
		
		while (!postShowActions.isEmpty())
			postShowActions.pop().run();
	}
	
	protected GameService getGameService() {
		
		return gameService;
	}
	
	public SkinService getSkinService() {
		
		return skinService;
	}
	
	public GameAssetService getAssetService() {
		
		return assetService;
	}
	
	/**
	 * If your implementation needs to define its own {@link InputProcessor}, it
	 * should override this method.
	 * <p>
	 * The default implementation simply returns {@code null} (signifying no such
	 * InputProcessor).
	 * </p>
	 * 
	 * @return
	 */
	protected InputProcessor getInputProcessor() {
		
		return null;
	}
	
	/**
	 * Returns your root Actor. Generally, you will want to defer construction of
	 * your root Actor to this method, as it may be called repeatedly (e.g., if the
	 * interface needs to be re-built).
	 * 
	 * @return null if no root-actor, or if you will manage your root actor(s)
	 *         manually
	 */
	protected abstract Actor getRoot();
	
	/**
	 * This default implementation simply calls, in order:
	 * <ol>
	 * <li>{@link #beforeStageAct(float) beforeStageAct()}</li>
	 * <li>{@link Stage#act() stage.act()}</li>
	 * <li>{@link #renderBeforeStage(float) renderBeforeStage()}</li>
	 * <li>{@link Stage#draw() stage.draw()}</li>
	 * <li>{@link #renderAfterStage(float) renderAfterStage()}</li>
	 * </ol>
	 * 
	 * @param delta
	 */
	@Override
	public void render(float delta) {
		
		if (clearScreen)
			clearScreen();
		
		if (gameService.getState().getModuleExceptionRegistry().hasUnreportedFailure()
				&& !exceptionReportingWindow.isVisible()) {
			exceptionReportingWindow
					.setFailure(gameService.getState().getModuleExceptionRegistry().peekCurrentFailure());
			exceptionReportingWindow.setVisible(true);
		}
		
		console.act(delta);
		
		beforeStageAct(delta);
		
		stage.act(delta);
		
		renderBeforeStage(delta);
		
		stage.draw();
		
		renderAfterStage(delta);
		
		console.render();
		
		fpsLabel.setVisible(gameService.getState().isShowFPS());
		if (gameService.getState().isShowFPS())
			fpsLabel.setText(Gdx.graphics.getFramesPerSecond() + " fps");
		
	}
	
	public abstract void beforeStageAct(float delta);
	
	public abstract void renderBeforeStage(float delta);
	
	public abstract void renderAfterStage(float delta);
	
	@Override
	public void resize(int width, int height) {
		
		stage.getViewport().update(width, height, true);
		console.resize(width, height);
		
		fpsLabel.setPosition(15, 15);
		
		exceptionReportingWindow.setPosition(width / 2, height / 2, Align.center);
	}
	
	@Override
	public void hide() {
		
		if (root != null)
			root.remove();
		
		fpsLabel.remove();
		
		exceptionReportingWindow.remove();
	}
	
	@Override
	public void dispose() {
		
	}
	
	public Stage getStage() {
		
		return stage;
	}
}
