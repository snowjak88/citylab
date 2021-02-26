/**
 * 
 */
package org.snowjak.city.screen.impl;

import org.snowjak.city.screen.AbstractScreen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * @author snowjak88
 *
 */
@Component
public class GreenScreen extends AbstractScreen {
	
	private static final Logger LOG = LoggerService.forClass(GreenScreen.class);
	
	private Batch batch = new SpriteBatch();
	private ShapeDrawer drawer = new ShapeDrawer(batch,
			new TextureRegion(new Texture("images/tilesets/world/default/landscapeTiles_000.png")));
	
	public GreenScreen() {
		
		super(5, 5, new InputListener() {
			
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				
				LOG.info("Touch DOWN @ [{0}, {1}]", x, y);
				return false;
			}
		});
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
