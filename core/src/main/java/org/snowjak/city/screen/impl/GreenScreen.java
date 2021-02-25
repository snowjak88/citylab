/**
 * 
 */
package org.snowjak.city.screen.impl;

import org.snowjak.city.screen.Screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * @author snowjak88
 *
 */
public class GreenScreen extends Screen {
	
	private final Logger log = LoggerService.forClass(GreenScreen.class);
	private Batch batch = new SpriteBatch();
	private ShapeDrawer drawer = new ShapeDrawer(batch,
			new TextureRegion(new Texture("images/tilesets/world/default/landscapeTiles_000.png")));
	
	public GreenScreen() {
		
		super(5, 5);
	}
	
	@Override
	public void render(float delta) {
		
		getViewport().apply(true);
		batch.setProjectionMatrix(getViewport().getCamera().combined);
		
		batch.begin();
		drawer.setColor(Color.GREEN);
		drawer.filledCircle(2.5f, 2.5f, 2.5f);
		batch.end();
	}
}