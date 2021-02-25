/**
 * 
 */
package org.snowjak.city.screen;

import org.snowjak.city.controller.MainScreenController;

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
public abstract class Screen {
	
	private final Viewport viewport;
	private ScreenTransitionHandler handler;
	
	public Screen(float worldWidth, float worldHeight) {
		
		this.viewport = new FitViewport(worldWidth, worldHeight);
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
	public Viewport getViewport() {
		
		return viewport;
	}
	
	/**
	 * When this Screen needs to be replaced with another Screen, it should use this
	 * {@link ScreenTransitionHandler}.
	 * 
	 * @param handler
	 */
	public void setScreenTransitionHandler(ScreenTransitionHandler handler) {
		
		this.handler = handler;
	}
	
	/**
	 * Attempt to transition to the given {@link Screen}. If this {@link Screen} has
	 * not received a {@link ScreenTransitionHandler}, this method will do nothing.
	 * 
	 * @param nextScreen
	 * @throws IllegalArgumentException
	 *             if {@code nextScreen} is {@code null}
	 */
	public void transitionToScreen(Screen nextScreen) {
		
		if (nextScreen == null)
			throw new IllegalArgumentException("Cannot transition to a null Screen!");
		
		if (handler != null)
			handler.handle(nextScreen);
	}
}
