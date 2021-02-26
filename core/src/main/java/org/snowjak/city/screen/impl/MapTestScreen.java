/**
 * 
 */
package org.snowjak.city.screen.impl;

import org.snowjak.city.screen.AbstractScreen;

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.asset.AssetService;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

/**
 * @author snowjak88
 *
 */
@Component
public class MapTestScreen extends AbstractScreen {
	
	private static final Logger LOG = LoggerService.forClass(MapTestScreen.class);
	
	@Inject
	private AssetService assetService;
	
	private TiledMap map;
	
	private SpriteBatch batch;
	private IsometricTiledMapRenderer renderer;
	
	private float dragStartX = 0, dragStartY = 0;
	private float cameraOffsetX = 0, cameraOffsetY = 0;
	private final Vector2 dragScratch = new Vector2();
	
	public MapTestScreen() {
		
		super(16, 16);
		
		batch = new SpriteBatch();
	}
	
	@Override
	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
		
		dragScratch.set(x, y);
		final Vector2 dragTo = getViewport().unproject(dragScratch);
		dragStartX = dragTo.x;
		dragStartY = dragTo.y;
		
		LOG.info("Dragging from [{0},{1}]", dragStartX, dragStartY);
		
		return true;
	}
	
	@Override
	public void touchDragged(InputEvent event, float x, float y, int pointer) {
		
		dragScratch.set(x, y);
		final Vector2 dragTo = getViewport().unproject(dragScratch);
		cameraOffsetX += (dragStartX - dragTo.x) * 0.5f;
		cameraOffsetY += -(dragStartY - dragTo.y) * 0.5f;
		
		LOG.info("Dragging from [{0},{1}] to [{2},{3}]", dragStartX, dragStartY, dragTo.x, dragTo.y);
		
		dragStartX = dragTo.x;
		dragStartY = dragTo.y;
	}
	
	@Override
	public void added() {
		
		//
		// Configure an AssetLoader allowing us to handle loading .tmx tile-map
		// definitions, as saved by the "Tiled" map-editor.
		assetService.getEagerAssetManager().setLoader(TiledMap.class, ".tmx",
				new TmxMapLoader(new InternalFileHandleResolver()));
		
		assetService.getEagerAssetManager().load("maps/untitled.tmx", TiledMap.class);
		assetService.getEagerAssetManager().finishLoadingAsset("maps/untitled.tmx");
		map = assetService.getEagerAssetManager().get("maps/untitled.tmx", TiledMap.class);
		
		renderer = new IsometricTiledMapRenderer(map, 1f / 64f, batch);
	}
	
	@Override
	public void render(float delta) {
		
		getViewport().apply();
		getViewport().getCamera().position.set(cameraOffsetX, cameraOffsetY, 0);
		getViewport().getCamera().update();
		
		batch.setProjectionMatrix(getViewport().getCamera().combined);
		
		renderer.setView((OrthographicCamera) getViewport().getCamera());
		renderer.render();
	}
	
}
