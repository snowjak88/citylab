/**
 * 
 */
package org.snowjak.city.screen;

import org.snowjak.city.controller.MainScreenController;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
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
public interface Screen {
	
	/**
	 * Render this Screen.
	 * <p>
	 * The calling {@link MainScreenController} will take care of all the busy-work
	 * before calling this method:
	 * <ul>
	 * <li>applying this Screen's {@link Viewport} (<b>centering</b> the configured
	 * Camera)</li>
	 * <li>ending any pre-existing {@link Batch}es</li>
	 * </ul>
	 * </p>
	 * 
	 * @param delta
	 *            time (in seconds) elapsed since last call to render()
	 */
	public void render(float delta);
	
	/**
	 * Return this Screen's {@link Viewport}. The {@link MainScreenController} will
	 * handle configuring this Viewport's "screen" characteristics. This Screen
	 * should handle configuring this Viewport's "world" characteristics.
	 * 
	 * @return
	 */
	public Viewport getViewport();
	
	/**
	 * Return this Screen's {@link InputProcessor} (or {@code null} if it doesn't
	 * require one).
	 * 
	 * @return
	 */
	public InputProcessor getInputProcessor();
	
	/**
	 * When this Screen needs to be replaced with another Screen, it should use this
	 * {@link ScreenTransitionHandler}.
	 * 
	 * @param handler
	 */
	public void setScreenTransitionHandler(ScreenTransitionHandler handler);
}
