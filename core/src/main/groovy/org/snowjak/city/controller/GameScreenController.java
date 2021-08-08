/**
 * 
 */
package org.snowjak.city.controller;

import static org.snowjak.city.util.Util.max;
import static org.snowjak.city.util.Util.min;

import org.snowjak.city.GameData;
import org.snowjak.city.GameData.GameParameters;
import org.snowjak.city.input.DragEventReceiver;
import org.snowjak.city.input.GameInputProcessor;
import org.snowjak.city.input.ScrollEventReceiver;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.map.renderer.MapRenderer;
import org.snowjak.city.module.Module;
import org.snowjak.city.service.MapGeneratorService;
import org.snowjak.city.service.TileSetService;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
 * Takes care of:
 * 
 * <ul>
 * <li>Managing the game-display</li>
 * <li>Organizes input-handling for game-{@link Module}s.</li>
 * </ul>
 * 
 * @author snowjak88
 *
 */
@View(id = "gameScreen", value = "ui/templates/gameScreen.lml")
public class GameScreenController implements ViewInitializer, ViewShower, ViewRenderer, ViewResizer, ActionContainer {
	
	private static final Logger LOG = LoggerService.forClass(GameScreenController.class);
	
	@Inject
	private MapGeneratorService mapGeneratorService;
	
	@Inject
	private TileSetService tileSetService;
	
	private final GameInputProcessor inputProcessor = new GameInputProcessor();
	private final Viewport viewport = new FitViewport(8, 8);
	private final SpriteBatch batch = new SpriteBatch();
	
	private MapRenderer renderer;
	
	private float cameraOffsetX, cameraOffsetY;
	
	float minWorldX, minWorldY, maxWorldX, maxWorldY;
	
	@Override
	public void initialize(Stage stage, ObjectMap<String, Actor> actorMappedByIds) {
		
		final GameData data = GameData.get();
		if (data.parameters == null)
			data.parameters = new GameParameters();
		
		final GameParameters param = data.parameters;
		
		data.tileset = (param.selectedTileset != null) ? param.selectedTileset
				: tileSetService.getTileSet(param.selectedTilesetName);
		
		if (param.seed != null && !param.seed.isEmpty())
			data.seed = param.seed;
		
		final MapGenerator generator = (param.selectedMapGenerator != null) ? param.selectedMapGenerator
				: mapGeneratorService.getGenerator(param.selectedMapGeneratorName);
		
		generator.setSeed(data.seed);
		data.map = generator.generate(param.mapWidth, param.mapHeight);
		data.map.updateTiles();
		
		//
		//
		//
		
		renderer = new MapRenderer(data.map, batch);
		
		final Vector2 scratch = new Vector2();
		scratch.set(0, 0);
		final Vector3 worldBound1 = renderer.translateIsoToScreen(scratch).cpy();
		
		scratch.set(0, param.mapHeight);
		final Vector3 worldBound2 = renderer.translateIsoToScreen(scratch).cpy();
		
		scratch.set(param.mapWidth, 0);
		final Vector3 worldBound3 = renderer.translateIsoToScreen(scratch).cpy();
		
		scratch.set(param.mapWidth, param.mapHeight);
		final Vector3 worldBound4 = renderer.translateIsoToScreen(scratch).cpy();
		
		minWorldX = min(worldBound1.x, worldBound2.x, worldBound3.x, worldBound4.x);
		minWorldY = min(worldBound1.y, worldBound2.y, worldBound3.y, worldBound4.y);
		maxWorldX = max(worldBound1.x, worldBound2.x, worldBound3.x, worldBound4.x);
		maxWorldY = max(worldBound1.y, worldBound2.y, worldBound3.y, worldBound4.y);
		
		//
		// Register the base map-control input-receivers.
		//
		inputProcessor.register(new DragEventReceiver() {
			
			Vector2 scratch = new Vector2();
			boolean ongoing = false;
			float startX = 0, startY = 0;
			
			@Override
			public void dragStart(int screenX, int screenY, int button) {
				
				if (button != Input.Buttons.LEFT) {
					ongoing = false;
					return;
				}
				
				setStart(screenX, screenY);
				ongoing = true;
			}
			
			@Override
			public void dragUpdate(int screenX, int screenY) {
				
				if (!ongoing)
					return;
				
				scrollMap(screenX, screenY);
				setStart(screenX, screenY);
			}
			
			@Override
			public void dragEnd(int screenX, int screenY) {
				
				if (!ongoing)
					return;
				
				scrollMap(screenX, screenY);
				ongoing = false;
			}
			
			private void setStart(int screenX, int screenY) {
				
				scratch.set(screenX, screenY);
				viewport.unproject(scratch);
				startX = scratch.x;
				startY = scratch.y;
			}
			
			private void scrollMap(int screenX, int screenY) {
				
				scratch.set(screenX, screenY);
				viewport.unproject(scratch);
				final float endX = scratch.x, endY = scratch.y;
				
				cameraOffsetX = min(max(cameraOffsetX + (endX - startX) / 2f, minWorldX), maxWorldX);
				cameraOffsetY = min(max(cameraOffsetY + (endY - startY) / 2f, minWorldY), maxWorldY);
			}
		});
		
		inputProcessor.register(new ScrollEventReceiver() {
			
			@Override
			public void scroll(float amountX, float amountY) {
				
				if (amountY == 0)
					return;
				
				if (amountY < 0)
					zoomOut();
				else
					zoomIn();
			}
			
			private void zoomOut() {
				
				final float newZoom = max(min(((OrthographicCamera) viewport.getCamera()).zoom / 2f, 8f), 1f);
				((OrthographicCamera) viewport.getCamera()).zoom = newZoom;
			}
			
			private void zoomIn() {
				
				final float newZoom = max(min(((OrthographicCamera) viewport.getCamera()).zoom * 2f, 8f), 1f);
				((OrthographicCamera) viewport.getCamera()).zoom = newZoom;
			}
		});
	}
	
	@Override
	public void show(Stage stage, Action action) {
		
		stage.addAction(Actions.sequence(action,
				Actions.run(() -> Gdx.input.setInputProcessor(new InputMultiplexer(stage, inputProcessor)))));
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
}
