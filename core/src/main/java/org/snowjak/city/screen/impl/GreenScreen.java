/**
 * 
 */
package org.snowjak.city.screen.impl;

import org.snowjak.city.screen.Screen;
import org.snowjak.city.screen.ScreenTransitionHandler;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * @author snowjak88
 *
 */
public class GreenScreen implements Screen {
	
	private Viewport viewport = new FitViewport(5, 5, new OrthographicCamera(5, 5));
	private Batch batch = new SpriteBatch();
	private ShapeDrawer drawer = new ShapeDrawer(batch,
			new TextureRegion(new Texture("images/tilesets/world/default/landscapeTiles_000.png")));
	private ScreenTransitionHandler handler;
	
	@Override
	public void render(float delta) {
		
		batch.setProjectionMatrix(viewport.getCamera().combined);
		
		batch.begin();
		drawer.setColor(Color.GREEN);
		drawer.filledCircle(2.5f, 2.5f, 2.5f);
		batch.end();
	}
	
	@Override
	public Viewport getViewport() {
		
		return viewport;
	}
	
	@Override
	public InputProcessor getInputProcessor() {
		
		return null;
	}
	
	@Override
	public void setScreenTransitionHandler(ScreenTransitionHandler handler) {
		
		this.handler = handler;
	}
}
