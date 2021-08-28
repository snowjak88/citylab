/**
 * 
 */
package org.snowjak.city.screens;

import java.util.LinkedList;

import org.snowjak.city.configuration.Configuration;
import org.snowjak.city.console.Console;
import org.snowjak.city.service.GameService;
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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.czyzby.kiwi.util.gdx.GdxUtilities;

/**
 * @author snowjak88
 *
 */
public abstract class AbstractGameScreen extends ScreenAdapter {
	
	public static final float SCREEN_FADE_TIME = 0.4f;
	
	private final LinkedList<Runnable> postShowActions = new LinkedList<>();
	
	private final GameService gameService;
	private final Console console;
	private final SkinService skinService;
	private final Stage stage;
	private Actor root;
	
	private Skin skin;
	private Color backgroundColor;
	
	private Game game;
	
	public AbstractGameScreen(GameService gameService, Console console, SkinService skinService, Stage stage) {
		
		this.gameService = gameService;
		this.console = console;
		this.stage = stage;
		this.skinService = skinService;
	}
	
	public void setGame(Game game) {
		
		this.game = game;
	}
	
	public void changeScreen(Screen screen) {
		
		stage.getRoot().addAction(
				Actions.sequence(Actions.fadeOut(SCREEN_FADE_TIME), Actions.run(() -> this.game.setScreen(screen))));
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
		
		skin = skinService.getSkin(Configuration.SKIN_NAME);
		if (skinService.getSkin(Configuration.SKIN_NAME).has("background", Color.class))
			backgroundColor = skin.get("background", Color.class);
		else
			backgroundColor = null;
		
		root = getRoot();
		if (root != null) {
			stage.getRoot().addActor(root);
			root.getColor().a = 0;
			
			root.addAction(Actions.fadeIn(SCREEN_FADE_TIME));
		}
		
		while (!postShowActions.isEmpty())
			postShowActions.pop().run();
	}
	
	protected GameService getGameService() {
		
		return gameService;
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
	 * @return your root Actor here, whether pre-constructed or built on the spot
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
		
		if (backgroundColor != null)
			GdxUtilities.clearScreen(backgroundColor.r, backgroundColor.g, backgroundColor.b);
		else
			GdxUtilities.clearScreen();
		
		console.act(delta);
		
		beforeStageAct(delta);
		
		stage.act(delta);
		
		renderBeforeStage(delta);
		
		stage.draw();
		
		renderAfterStage(delta);
		
		console.render();
	}
	
	public abstract void beforeStageAct(float delta);
	
	public abstract void renderBeforeStage(float delta);
	
	public abstract void renderAfterStage(float delta);
	
	@Override
	public void resize(int width, int height) {
		
		stage.getViewport().update(width, height, true);
		console.resize(width, height);
	}
	
	@Override
	public void hide() {
		
		if (root != null)
			root.remove();
	}
	
	@Override
	public void dispose() {
		
	}
	
	public Stage getStage() {
		
		return stage;
	}
}
