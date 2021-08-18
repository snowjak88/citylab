/**
 * 
 */
package org.snowjak.city.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.Provider;
import com.github.czyzby.autumn.provider.DependencyProvider;

/**
 * @author snowjak88
 *
 */
@Provider
public class StageProvider implements DependencyProvider<Stage> {
	
	@Inject(newInstance = true)
	private Viewport viewport;
	
	@Override
	public Class<Stage> getDependencyType() {
		
		return Stage.class;
	}
	
	@Override
	public Stage provide() {
		
		return new Stage(viewport);
	}
	
}
