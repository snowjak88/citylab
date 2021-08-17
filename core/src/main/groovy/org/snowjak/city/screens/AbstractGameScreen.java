/**
 * 
 */
package org.snowjak.city.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.github.czyzby.kiwi.util.gdx.GdxUtilities;

/**
 * @author snowjak88
 *
 */
public abstract class AbstractGameScreen extends ScreenAdapter {
	
	public static final float SCREEN_FADE_TIME = 0.4f;
	
	private final Stage stage;
	private Actor root;
	
	private Game game;
	
	public AbstractGameScreen(Stage stage) {
		
		this.stage = stage;
	}
	
	public void setGame(Game game) {
		
		this.game = game;
	}
	
	public void changeScreen(Screen screen) {
		
		stage.getRoot().addAction(
				Actions.sequence(Actions.fadeOut(SCREEN_FADE_TIME), Actions.run(() -> this.game.setScreen(screen))));
	}
	
	/**
	 * Performs the following steps:
	 * <ol>
	 * <li>Sets the configured {@link Stage} as the active
	 * {@link InputProcessor}</li>
	 * <li>Calls {@link #getRoot()} to get this screen's root {@link Actor}</li>
	 * <li>Schedules that root to fade in over {@link #SCREEN_FADE_TIME} seconds
	 * </li>
	 * </ol>
	 * 
	 * If you need to set up an {@link InputMultiplexer} between your own
	 * {@link InputProcessor} and the Stage, you should override this method:
	 * 
	 * <pre>
	 * 
	 * &#64;Override
	 * public void show() {
	 * 	
	 * 	super.show();
	 * 	
	 * 	Gdx.input.setInputProcessor(new InputMultiplexer(getStage(), myInputProcessor));
	 * }
	 * </pre>
	 */
	@Override
	public void show() {
		
		Gdx.input.setInputProcessor(stage);
		
		root = getRoot();
		if (root != null) {
			stage.getRoot().addActor(root);
			root.getColor().a = 0;
			
			root.addAction(Actions.fadeIn(SCREEN_FADE_TIME));
		}
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
		
		GdxUtilities.clearScreen();
		
		beforeStageAct(delta);
		
		stage.act(delta);
		
		renderBeforeStage(delta);
		
		stage.draw();
		
		renderAfterStage(delta);
	}
	
	public abstract void beforeStageAct(float delta);
	
	public abstract void renderBeforeStage(float delta);
	
	public abstract void renderAfterStage(float delta);
	
	@Override
	public void resize(int width, int height) {
		
		stage.getViewport().update(width, height, true);
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
