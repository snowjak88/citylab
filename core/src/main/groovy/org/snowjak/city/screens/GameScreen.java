/**
 * 
 */
package org.snowjak.city.screens;

import static org.snowjak.city.util.Util.max;
import static org.snowjak.city.util.Util.min;

import java.util.LinkedHashSet;
import java.util.Set;

import org.snowjak.city.GameState;
import org.snowjak.city.configuration.InitPriority;
import org.snowjak.city.console.Console;
import org.snowjak.city.input.GameInputProcessor;
import org.snowjak.city.input.KeyTypedEvent;
import org.snowjak.city.input.ScreenDragEndEvent;
import org.snowjak.city.input.ScreenDragStartEvent;
import org.snowjak.city.input.ScreenDragUpdateEvent;
import org.snowjak.city.input.ScrollEvent;
import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.renderer.MapRenderer;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;
import org.snowjak.city.service.SkinService;
import org.snowjak.city.tools.Tool;
import org.snowjak.city.tools.ui.Toolbar;
import org.snowjak.city.util.UnregistrationHandle;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.kiwi.log.Logger;

/**
 * Presents the actual game-screen.
 * 
 * @author snowjak88
 *
 */
@Component
public class GameScreen extends AbstractGameScreen {
	
	private static final Logger LOG = LoggerService.forClass(GameScreen.class);
	
	private static final float MIN_ZOOM = 0.25f, MAX_ZOOM = 4f;
	
	private final MainMenuScreen mainMenuScreen;
	private final I18NService i18nService;
	
	public GameScreen(GameService gameService, Console console, I18NService i18nService, SkinService skinService,
			GameAssetService assetService, Stage stage, MainMenuScreen mainMenuScreen) {
		
		super(gameService, console, i18nService, skinService, assetService, stage);
		
		this.i18nService = i18nService;
		this.mainMenuScreen = mainMenuScreen;
		this.renderer = new MapRenderer(gameService.getState());
		
		this.setBackgroundColor(Color.BLACK);
	}
	
	private GameInputProcessor inputProcessor;
	private final ScreenViewport viewport = new ScreenViewport();
	{
		viewport.setUnitsPerPixel(1f / MapRenderer.WORLD_GRID_UNIT_SIZE);
	}
	
	private float cameraOffsetX, cameraOffsetY;
	private boolean cameraUpdated = true;
	
	private final MapRenderer renderer;
	float minWorldX, minWorldY, maxWorldX, maxWorldY;
	
	private Toolbar buttonList;
	private GameScreenInputHandler inputHandler;
	
	private Set<UnregistrationHandle> inputUnregistrations = new LinkedHashSet<>();
	
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
		
		buttonList = new Toolbar(i18nService, getSkinService(), getGameService(), getAssetService(),
				() -> getStage().setScrollFocus(null));
		
		buttonList.setPosition(0, getStage().getHeight(), Align.topLeft);
		
		return buttonList;
	}
	
	@Override
	public void show() {
		
		super.show();
		
		//
		//
		//
		
		inputHandler = new GameScreenInputHandler();
		
		final GameState state = getGameService().getState();
		
		state.setToolbar(buttonList);
		state.setCamera(getCameraControl());
		state.setInputProcessor(inputProcessor);
		
		inputUnregistrations.add(inputProcessor.register(KeyTypedEvent.class, state.getHotkeys()));
		
		final CityMap map = state.getMap();
		
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
		
		inputUnregistrations.add(inputProcessor.register(ScreenDragStartEvent.class,
				e -> inputHandler.dragStart(e.getX(), e.getY(), e.getButton())));
		inputUnregistrations.add(
				inputProcessor.register(ScreenDragUpdateEvent.class, e -> inputHandler.dragUpdate(e.getX(), e.getY())));
		inputUnregistrations
				.add(inputProcessor.register(ScreenDragEndEvent.class, e -> inputHandler.dragEnd(e.getX(), e.getY())));
		inputUnregistrations.add(
				inputProcessor.register(ScrollEvent.class, e -> inputHandler.scroll(e.getAmountX(), e.getAmountY())));
		
		state.getModules().forEach((id, module) -> {
			if (!module.getActivated())
				module.getOnActivationActions().forEach(Runnable::run);
		});
	}
	
	@Override
	public void hide() {
		
		super.hide();
		
		final GameState state = getGameService().getState();
		
		state.setCamera(null);
		state.setInputProcessor(null);
		state.setToolbar(null);
		
		inputUnregistrations.forEach(UnregistrationHandle::unregisterMe);
		inputUnregistrations.clear();
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
		
		if (renderer != null) {
			
			if (cameraUpdated) {
				
				viewport.getCamera().position.set(cameraOffsetX, cameraOffsetY, 0);
				viewport.getCamera().update();
				renderer.setView((OrthographicCamera) viewport.getCamera());
				
				cameraUpdated = false;
			}
			
			viewport.apply();
			
			renderer.render(delta);
			
			//
			// Remember to re-apply the Stage's viewport.
			getStage().getViewport().apply();
		}
	}
	
	@Override
	public void renderAfterStage(float delta) {
		
		final Tool activeTool = getGameService().getState().getActiveTool();
		if (activeTool != null)
			activeTool.update();
	}
	
	@Override
	public void resize(int width, int height) {
		
		super.resize(width, height);
		viewport.update(width, height);
		cameraUpdated = true;
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
		
		/**
		 * Set the camera's zoom-factor. 1.0 = normal, 2.0 = double-size, 0.5 =
		 * half-size, etc.
		 * 
		 * @param zoomLevel
		 */
		public void setZoom(float zoomLevel);
		
		/**
		 * Get the camera's current zoom-factor. 1.0 = normal, 2.0 = double-size, 0.5 =
		 * half-size, etc.
		 * 
		 * @return
		 */
		public float getZoom();
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
			
			if (button != Input.Buttons.RIGHT) {
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
			
			setZoom(getZoom() / 2f);
		}
		
		private void zoomIn() {
			
			setZoom(getZoom() * 2f);
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
		
		@Override
		public void setZoom(float zoomLevel) {
			
			final float newZoom = max(min(zoomLevel, MAX_ZOOM), MIN_ZOOM);
			((OrthographicCamera) viewport.getCamera()).zoom = newZoom;
			cameraUpdated = true;
		}
		
		@Override
		public float getZoom() {
			
			return ((OrthographicCamera) viewport.getCamera()).zoom;
		}
		
	}
}
