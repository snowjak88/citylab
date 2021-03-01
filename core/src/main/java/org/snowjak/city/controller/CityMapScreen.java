/**
 * 
 */
package org.snowjak.city.controller;

import static java.lang.Math.max;
import static java.lang.Math.min;

import org.snowjak.city.map.TileSet;
import org.snowjak.city.map.TileSetLoader;

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.asset.AssetService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewController;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewInitializer;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewRenderer;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewResizer;
import com.github.czyzby.autumn.mvc.stereotype.View;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;
import com.github.czyzby.lml.parser.action.ActionContainer;

/**
 * Allows the user to interact with the map of the city.
 * 
 * @author snowjak88
 *
 */
@View(id = "cityMapScreen", value = "ui/templates/cityMapScreen.lml")
public class CityMapScreen implements ViewInitializer, ViewRenderer, ViewResizer, ActionContainer {
	
	private static final Logger LOG = LoggerService.forClass(CityMapScreen.class);
	
	@Inject
	private AssetService assetService;
	
	@Inject
	private InterfaceService interfaceService;
	
	private Viewport viewport = new FitViewport(8, 8);
	private SpriteBatch batch = new SpriteBatch();
	
	private TiledMap map = null;
	private IsometricTiledMapRenderer renderer;
	
	private float cameraOffsetX, cameraOffsetY;
	
	@Override
	public void initialize(Stage stage, ObjectMap<String, Actor> actorMappedByIds) {
		
		final TileSetLoader tileSetLoader = new TileSetLoader(new InternalFileHandleResolver());
		assetService.getAssetManager().setLoader(TileSet.class, ".json", tileSetLoader);
		assetService.getEagerAssetManager().setLoader(TileSet.class, ".json", tileSetLoader);
		
		//
		//
		//
		
		assetService.load("images/tilesets/terrain/default/tileset.json", TileSet.class);
		
		assetService.finishLoading("images/tilesets/terrain/default/tileset.json", TileSet.class);
		
		final TileSet tileset = assetService.get("images/tilesets/terrain/default/tileset.json", TileSet.class);
		
		final int worldWidthInTiles = 16, worldHeightInTiles = 16;
		final TiledMapTileLayer layer = new TiledMapTileLayer(worldWidthInTiles, worldHeightInTiles,
				tileset.getTileSetDescriptor().getGridWidth(), tileset.getTileSetDescriptor().getGridHeight());
		layer.setName("base");
		
		map = new TiledMap();
		map.getTileSets().addTileSet(tileset);
		map.getLayers().add(layer);
		
		for (int x = 0; x < layer.getWidth(); x++)
			for (int y = 0; y < layer.getHeight(); y++) {
				final TiledMapTile tile = tileset.getRandomTile("Grass");
				final TiledMapTileLayer.Cell cell = new Cell();
				cell.setTile(tile);
				layer.setCell(x, y, cell);
			}
		
		renderer = new IsometricTiledMapRenderer(map, 1f / (float) tileset.getTileSetDescriptor().getWidth(), batch);
		
		stage.addListener(new InputListener() {
			
			private final Vector2 scratch = new Vector2();
			private float startX = 0, startY = 0;
			
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				
				scratch.set(x, y);
				final Vector2 worldCoords = viewport.unproject(scratch);
				startX = worldCoords.x;
				startY = worldCoords.y;
				return true;
			}
			
			@Override
			public void touchDragged(InputEvent event, float x, float y, int pointer) {
				
				scratch.set(x, y);
				final Vector2 worldCoords = viewport.unproject(scratch);
				
				cameraOffsetX += (worldCoords.x - startX) / 2f;
				cameraOffsetY -= (worldCoords.y - startY) / 2f;
				
				startX = worldCoords.x;
				startY = worldCoords.y;
				
				cameraOffsetX = min(max(cameraOffsetX, 0f), worldWidthInTiles);
				cameraOffsetY = min(max(cameraOffsetY, 0f), worldHeightInTiles);
			}
			
		});
	}
	
	@Override
	public void render(Stage stage, float delta) {
		
		stage.act(delta);
		
		if (renderer != null) {
			viewport.apply();
			
			viewport.getCamera().position.set(cameraOffsetX, cameraOffsetY, 0);
			viewport.getCamera().update();
			renderer.setView((OrthographicCamera) viewport.getCamera());
			
			renderer.render();
			
			stage.getViewport().apply();
		}
		
		stage.draw();
	}
	
	@Override
	public void resize(Stage stage, int width, int height) {
		
		viewport.update(width, height);
	}
	
	@Override
	public void destroy(ViewController viewController) {
		
	}
}
