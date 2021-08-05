/**
 * 
 */
package org.snowjak.city.controller;

import static java.lang.Math.pow;
import static org.snowjak.city.util.Util.max;
import static org.snowjak.city.util.Util.min;

import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.map.renderer.MapRenderer;
import org.snowjak.city.service.MapGeneratorService;
import org.snowjak.city.service.TileSetService;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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
	
	@Inject
	private MapGeneratorService mapGeneratorService;
	
	@Inject
	private TileSetService tileSetService;
	
	private Viewport viewport = new FitViewport(8, 8);
	private SpriteBatch batch = new SpriteBatch();
	
	private CityMap map = null;
	private MapRenderer renderer;
	
	private float cameraOffsetX, cameraOffsetY;
	
	@Override
	public void initialize(Stage stage, ObjectMap<String, Actor> actorMappedByIds) {
		
		//
		//
		//
		
		final int worldWidthInTiles = 64, worldHeightInTiles = 64;
		
		final MapGenerator mapGenerator = mapGeneratorService.getGenerator("rolling-hills");
		map = mapGenerator.generate(worldWidthInTiles, worldHeightInTiles, tileSetService.getTileSet(), false, false);
		
		renderer = new MapRenderer(map, batch);
		
		final Vector2 scratch = new Vector2();
		scratch.set(0, 0);
		final Vector3 worldBound1 = renderer.translateIsoToScreen(scratch).cpy();
		
		scratch.set(0, worldHeightInTiles);
		final Vector3 worldBound2 = renderer.translateIsoToScreen(scratch).cpy();
		
		scratch.set(worldWidthInTiles, 0);
		final Vector3 worldBound3 = renderer.translateIsoToScreen(scratch).cpy();
		
		scratch.set(worldWidthInTiles, worldHeightInTiles);
		final Vector3 worldBound4 = renderer.translateIsoToScreen(scratch).cpy();
		
		final float minWorldX = min(worldBound1.x, worldBound2.x, worldBound3.x, worldBound4.x);
		final float minWorldY = min(worldBound1.y, worldBound2.y, worldBound3.y, worldBound4.y);
		final float maxWorldX = max(worldBound1.x, worldBound2.x, worldBound3.x, worldBound4.x);
		final float maxWorldY = max(worldBound1.y, worldBound2.y, worldBound3.y, worldBound4.y);
		
		stage.addListener(new InputListener() {
			
			private final Vector2 scratch = new Vector2();
			private float startDragX = 0, startDragY = 0;
			private float currentScrollY = 0;
			
			@Override
			public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
				
				currentScrollY += amountY;
				final float newZoom = max(min((float) pow(2f, currentScrollY), 4f), 1f / 8f);
				((OrthographicCamera) viewport.getCamera()).zoom = newZoom;
				
				return true;
			}
			
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				
				if (button == Input.Buttons.LEFT) {
					//
					// Start drag-scroll
					scratch.set(x, y);
					final Vector2 worldCoords = viewport.unproject(scratch);
					startDragX = worldCoords.x;
					startDragY = worldCoords.y;
				}
				
				return true;
			}
			
			@Override
			public void touchDragged(InputEvent event, float x, float y, int pointer) {
				
				scratch.set(x, y);
				final Vector2 worldCoords = viewport.unproject(scratch);
				
				cameraOffsetX += (worldCoords.x - startDragX) / 2f;
				cameraOffsetY -= (worldCoords.y - startDragY);
				
				startDragX = worldCoords.x;
				startDragY = worldCoords.y;
				
				cameraOffsetX = min(max(cameraOffsetX, minWorldX), maxWorldX);
				cameraOffsetY = min(max(cameraOffsetY, minWorldY), maxWorldY);
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
