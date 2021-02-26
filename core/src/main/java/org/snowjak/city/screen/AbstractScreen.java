/**
 * 
 */
package org.snowjak.city.screen;

import org.snowjak.city.controller.MainScreenController;

import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * The {@link MainScreenController} can display a Screen implementation
 * underneath its LML-templated bits.
 * <p>
 * {@link MainScreenController} will handle the
 * </p>
 * 
 * @author snowjak88
 *
 */
public abstract class AbstractScreen extends InputListener {
	
	private final Viewport viewport;
	private ScreenTransitionHandler handler;
	
	/**
	 * Construct a new Screen, configuring a {@link FitViewport}.
	 * 
	 * @param worldWidth
	 * @param worldHeight
	 */
	public AbstractScreen(float worldWidth, float worldHeight) {
		
		this(new FitViewport(worldWidth, worldHeight));
	}
	
	/**
	 * Construct a new Screen, configuring a custom {@link Viewport}.
	 * 
	 * @param viewport
	 * @param inputListener
	 */
	public AbstractScreen(Viewport viewport) {
		
		this.viewport = viewport;
	}
	
	/**
	 * Called when this Screen is added to the {@link MainScreenController}.
	 */
	public void added() {
		
	}
	
	/**
	 * Called when this Screen is removed from the {@link MainScreenController}.
	 */
	public void removed() {
		
	}
	
	/**
	 * Render this Screen.
	 * 
	 * @param delta
	 *            time (in seconds) elapsed since last call to render()
	 */
	public abstract void render(float delta);
	
	/**
	 * Return this Screen's {@link Viewport}.
	 * 
	 * @return
	 */
	public final Viewport getViewport() {
		
		return viewport;
	}
	
	/**
	 * When this Screen needs to be replaced with another Screen, it should use this
	 * {@link ScreenTransitionHandler}.
	 * 
	 * @param handler
	 */
	public final void setScreenTransitionHandler(ScreenTransitionHandler handler) {
		
		this.handler = handler;
	}
	
	/**
	 * Attempt to transition to the given {@link AbstractScreen}. If this
	 * {@link AbstractScreen} has not received a {@link ScreenTransitionHandler},
	 * this method will do nothing.
	 * 
	 * @param nextScreen
	 * @throws IllegalArgumentException
	 *             if {@code nextScreen} is {@code null}
	 */
	public final void transitionToScreen(AbstractScreen nextScreen) {
		
		if (nextScreen == null)
			throw new IllegalArgumentException("Cannot transition to a null Screen!");
		
		if (handler != null)
			handler.handle(nextScreen);
	}
}
