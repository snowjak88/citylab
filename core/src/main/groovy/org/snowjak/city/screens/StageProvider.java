/**
 * 
 */
package org.snowjak.city.screens;

import org.snowjak.city.CityGame;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.czyzby.autumn.annotation.Provider;
import com.github.czyzby.autumn.provider.DependencyProvider;

/**
 * @author snowjak88
 *
 */
@Provider
public class StageProvider implements DependencyProvider<Stage> {
	
	@Override
	public Class<Stage> getDependencyType() {
		
		return Stage.class;
	}
	
	@Override
	public Stage provide() {
		
		return new Stage(new FitViewport(CityGame.WIDTH, CityGame.HEIGHT));
	}
	
}
