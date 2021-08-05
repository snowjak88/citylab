/**
 * 
 */
package org.snowjak.city.controller;

import static org.snowjak.city.util.Util.max;
import static org.snowjak.city.util.Util.min;

import org.snowjak.city.input.GameInputProcessor;
import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.map.renderer.MapRenderer;
import org.snowjak.city.service.MapGeneratorService;
import org.snowjak.city.service.TileSetService;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewShower;
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
public class CityMapScreen implements ViewInitializer, ViewShower, ViewRenderer, ViewResizer, ActionContainer {
	
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
	
	float minWorldX, minWorldY, maxWorldX, maxWorldY;
	
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
		
		minWorldX = min(worldBound1.x, worldBound2.x, worldBound3.x, worldBound4.x);
		minWorldY = min(worldBound1.y, worldBound2.y, worldBound3.y, worldBound4.y);
		maxWorldX = max(worldBound1.x, worldBound2.x, worldBound3.x, worldBound4.x);
		maxWorldY = max(worldBound1.y, worldBound2.y, worldBound3.y, worldBound4.y);
	}
	
	@Override
	public void show(Stage stage, Action action) {
		
		stage.addAction(Actions.sequence(action, Actions.run(() -> {
			Gdx.input.setInputProcessor(new InputMultiplexer(stage, new GameInputProcessor(new ZoomControl() {
				
				@Override
				public void zoomOut() {
					
					final float newZoom = max(min(((OrthographicCamera) viewport.getCamera()).zoom / 2f, 4f), 1f / 8f);
					((OrthographicCamera) viewport.getCamera()).zoom = newZoom;
				}
				
				@Override
				public void zoomIn() {
					
					final float newZoom = max(min(((OrthographicCamera) viewport.getCamera()).zoom * 2f, 4f), 1f / 8f);
					((OrthographicCamera) viewport.getCamera()).zoom = newZoom;
				}
			}, new MapScrollControl() {
				
				Vector2 scratch = new Vector2();
				
				@Override
				public void scrollBy(int startX, int startY, int endX, int endY) {
					
					scratch.set(startX, startY);
					scratch = viewport.unproject(scratch);
					final float worldStartX = scratch.x, worldStartY = scratch.y;
					
					scratch.set(endX, endY);
					scratch = viewport.unproject(scratch);
					final float worldEndX = scratch.x, worldEndY = scratch.y;
					
					cameraOffsetX = min(max(cameraOffsetX + (worldEndX - worldStartX) / 2f, minWorldX), maxWorldX);
					cameraOffsetY = min(max(cameraOffsetY + (worldEndY - worldStartY), minWorldY), maxWorldY);
				}
			})));
		})));
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
		stage.getViewport().update(width, height, true);
	}
	
	@Override
	public void hide(Stage stage, Action action) {
		
		stage.addAction(action);
	}
	
	@Override
	public void destroy(ViewController viewController) {
		
	}
	
	public interface ZoomControl {
		
		public void zoomIn();
		
		public void zoomOut();
	}
	
	public interface MapScrollControl {
		
		public void scrollBy(int startX, int startY, int endX, int endY);
	}
}
