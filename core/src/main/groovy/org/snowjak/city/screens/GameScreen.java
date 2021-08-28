/**
 * 
 */
package org.snowjak.city.screens;

import static org.snowjak.city.util.Util.max;
import static org.snowjak.city.util.Util.min;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.snowjak.city.configuration.InitPriority;
import org.snowjak.city.console.Console;
import org.snowjak.city.input.GameInputProcessor;
import org.snowjak.city.input.MapHoverEvent;
import org.snowjak.city.input.ScreenDragEndEvent;
import org.snowjak.city.input.ScreenDragStartEvent;
import org.snowjak.city.input.ScreenDragUpdateEvent;
import org.snowjak.city.input.ScrollEvent;
import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.renderer.MapRenderer;
import org.snowjak.city.map.renderer.RenderingSupport;
import org.snowjak.city.map.renderer.hooks.AbstractCustomRenderingHook;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.GameService.GameState;
import org.snowjak.city.service.LoggerService;
import org.snowjak.city.service.SkinService;
import org.snowjak.city.util.RelativePriorityList.PrioritizationFailedException;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.util.gdx.GdxUtilities;

import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Presents the actual game-screen.
 * 
 * @author snowjak88
 *
 */
@Component
public class GameScreen extends AbstractGameScreen {
	
	private static final Logger LOG = LoggerService.forClass(GameScreen.class);
	
	private final MainMenuScreen mainMenuScreen;
	
	public GameScreen(GameService gameService, Console console, SkinService skinService, Stage stage,
			MainMenuScreen mainMenuScreen) {
		
		super(gameService, console, skinService, stage);
		
		this.mainMenuScreen = mainMenuScreen;
		this.renderer = gameService.getState().getRenderer();
	}
	
	private GameInputProcessor inputProcessor;
	private final Viewport viewport = new FitViewport(8, 8);
	
	private float cameraOffsetX, cameraOffsetY;
	private boolean cameraUpdated = true;
	
	private MapRenderer renderer;
	float minWorldX, minWorldY, maxWorldX, maxWorldY;
	
	private GameScreenInputHandler inputHandler;
	
	@Initiate(priority = InitPriority.LOWEST_PRIORITY)
	public void init() {
		
		//
		// Set up the base map-control input-receivers.
		//
		inputProcessor = new GameInputProcessor((s) -> {
			//
			// Screen- to viewport-coordinates ...
			final Vector2 tmp = new Vector2(s);
			viewport.unproject(tmp);
			
			//
			// ... then viewport- to map-coordinates ...
			return renderer.viewportToMap(tmp);
		});
	}
	
	@Override
	protected Actor getRoot() {
		
		return null;
	}
	
	@Override
	public void show() {
		
		super.show();
		
		//
		//
		//
		
		final GameState state = getGameService().getState();
		
		state.setCamera(getCameraControl());
		
		final CityMap map = state.getMap();
		final MapRenderer renderer = state.getRenderer();
		
		final Vector2 scratch = new Vector2();
		scratch.set(0, 0);
		final Vector2 worldBound1 = renderer.mapToViewport(scratch).cpy();
		
		scratch.set(0, map.getHeight());
		final Vector2 worldBound2 = renderer.mapToViewport(scratch).cpy();
		
		scratch.set(map.getWidth(), 0);
		final Vector2 worldBound3 = renderer.mapToViewport(scratch).cpy();
		
		scratch.set(map.getWidth(), map.getHeight());
		final Vector2 worldBound4 = renderer.mapToViewport(scratch).cpy();
		
		minWorldX = min(worldBound1.x, worldBound2.x, worldBound3.x, worldBound4.x);
		minWorldY = min(worldBound1.y, worldBound2.y, worldBound3.y, worldBound4.y);
		maxWorldX = max(worldBound1.x, worldBound2.x, worldBound3.x, worldBound4.x);
		maxWorldY = max(worldBound1.y, worldBound2.y, worldBound3.y, worldBound4.y);
		
		inputHandler = new GameScreenInputHandler();
		inputProcessor.register(ScreenDragStartEvent.class,
				e -> inputHandler.dragStart(e.getX(), e.getY(), e.getButton()));
		inputProcessor.register(ScreenDragUpdateEvent.class, e -> inputHandler.dragUpdate(e.getX(), e.getY()));
		inputProcessor.register(ScreenDragEndEvent.class, e -> inputHandler.dragEnd(e.getX(), e.getY()));
		inputProcessor.register(ScrollEvent.class, e -> inputHandler.scroll(e.getAmountX(), e.getAmountY()));
		
		final AtomicInteger hoverX = new AtomicInteger(0), hoverY = new AtomicInteger(0);
		final AtomicBoolean hoverActive = new AtomicBoolean(false);
		inputProcessor.register(MapHoverEvent.class, e -> {
			if (getGameService().getState().getMap() == null || !map.isValidCell(e.getX(), e.getY())) {
				hoverActive.set(false);
				return;
			}
			
			hoverActive.set(true);
			hoverX.set(e.getX());
			hoverY.set(e.getY());
		});
		final AbstractCustomRenderingHook hoverHook = new AbstractCustomRenderingHook("hover") {
			
			@Override
			public void render(float delta, Batch batch, ShapeDrawer shapeDrawer, RenderingSupport support) {
				
				if (!hoverActive.get())
					return;
				
				final Vector2[] vertices = support.getCellVertices(hoverX.get(), hoverY.get(), null);
				shapeDrawer.setColor(Color.WHITE);
				shapeDrawer.line(vertices[0], vertices[1]);
				shapeDrawer.line(vertices[1], vertices[2]);
				shapeDrawer.line(vertices[2], vertices[3]);
				shapeDrawer.line(vertices[3], vertices[0]);
			}
		};
		hoverHook.getRelativePriority().after("map");
		
		try {
			renderer.addCustomRenderingHook(hoverHook);
		} catch (PrioritizationFailedException e) {
			LOG.error("Cannot initialize GameScreen -- prioritization-failure while adding hover-handler!");
			changeScreen(mainMenuScreen);
		}
	}
	
	@Override
	public void hide() {
		
		super.hide();
		
		getGameService().getState().setCamera(null);
	}
	
	@Override
	protected InputProcessor getInputProcessor() {
		
		return inputProcessor;
	}
	
	public GameCameraControl getCameraControl() {
		
		return inputHandler;
	}
	
	@Override
	public void beforeStageAct(float delta) {
		
		final Engine entityEngine = getGameService().getState().getEngine();
		if (entityEngine != null)
			entityEngine.update(delta);
		
	}
	
	@Override
	public void renderBeforeStage(float delta) {
		
		GdxUtilities.clearScreen();
		
		if (renderer != null) {
			viewport.apply();
			
			if (cameraUpdated) {
				viewport.getCamera().position.set(cameraOffsetX, cameraOffsetY, 0);
				viewport.getCamera().update();
				renderer.setView((OrthographicCamera) viewport.getCamera());
				
				cameraUpdated = false;
			}
			
			renderer.render(delta);
			
			//
			// Remember to re-apply the Stage's viewport.
			getStage().getViewport().apply();
		}
	}
	
	@Override
	public void renderAfterStage(float delta) {
		
		//
		// nothing to do here
	}
	
	@Override
	public void resize(int width, int height) {
		
		super.resize(width, height);
		viewport.update(width, height);
	}
	
	/**
	 * Provides programmatic control over the game's camera.
	 * 
	 * @author snowjak88
	 *
	 */
	public interface GameCameraControl {
		
		/**
		 * Move the camera so it is centered above the given map-cell.
		 * 
		 * @param cellX
		 * @param cellY
		 */
		public void setCamera(int cellX, int cellY);
		
		/**
		 * Get the map-cell over which the camera is centered.
		 * 
		 * @return
		 */
		public Vector2 getCamera();
	}
	
	/**
	 * Ad-hoc interpreter for input-events.
	 * 
	 * @author snowjak88
	 *
	 */
	public class GameScreenInputHandler implements GameCameraControl {
		
		Vector2 scratch = new Vector2();
		boolean ongoing = false;
		float startX = 0, startY = 0;
		
		public void dragStart(int screenX, int screenY, int button) {
			
			if (button != Input.Buttons.LEFT) {
				ongoing = false;
				return;
			}
			
			setStart(screenX, screenY);
			ongoing = true;
		}
		
		public void dragUpdate(int screenX, int screenY) {
			
			if (!ongoing)
				return;
			
			scrollMap(screenX, screenY);
			setStart(screenX, screenY);
		}
		
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
			scrollMapToViewport(scratch.x, scratch.y);
		}
		
		private void scrollMapToViewport(float viewportX, float viewportY) {
			
			cameraOffsetX = min(max(cameraOffsetX + (viewportX - startX) / 2f, minWorldX), maxWorldX);
			cameraOffsetY = min(max(cameraOffsetY + (viewportY - startY) / 2f, minWorldY), maxWorldY);
			cameraUpdated = true;
		}
		
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
			cameraUpdated = true;
		}
		
		private void zoomIn() {
			
			final float newZoom = max(min(((OrthographicCamera) viewport.getCamera()).zoom * 2f, 8f), 1f);
			((OrthographicCamera) viewport.getCamera()).zoom = newZoom;
			cameraUpdated = true;
		}
		
		@Override
		public void setCamera(int cellX, int cellY) {
			
			scratch.set(cellX, cellY);
			renderer.mapToViewport(scratch);
			scrollMapToViewport(scratch.x, scratch.y);
		}
		
		@Override
		public Vector2 getCamera() {
			
			scratch.set(cameraOffsetX, cameraOffsetY);
			renderer.viewportToMap(scratch);
			return scratch.cpy();
		}
	}
}
