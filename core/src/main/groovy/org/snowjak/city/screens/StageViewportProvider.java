/**
 * 
 */
package org.snowjak.city.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.czyzby.autumn.annotation.Provider;
import com.github.czyzby.autumn.provider.DependencyProvider;

/**
 * {@link Stage} instances should use this common {@link Viewport} type.
 * 
 * @author snowjak88
 *
 */
@Provider
public class StageViewportProvider implements DependencyProvider<Viewport> {
	
	@Override
	public Class<Viewport> getDependencyType() {
		
		return Viewport.class;
	}
	
	@Override
	public Viewport provide() {
		
		final ScreenViewport viewport = new ScreenViewport();
		viewport.setUnitsPerPixel(1f);
		return viewport;
	}
	
}
