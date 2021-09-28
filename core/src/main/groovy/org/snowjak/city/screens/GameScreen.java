/**
 * 
 */
package org.snowjak.city.screens;

import static org.snowjak.city.util.Util.max;
import static org.snowjak.city.util.Util.min;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.snowjak.city.GameState;
import org.snowjak.city.configuration.InitPriority;
import org.snowjak.city.console.Console;
import org.snowjak.city.input.GameInputProcessor;
import org.snowjak.city.input.KeyDownEvent;
import org.snowjak.city.input.KeyTypedEvent;
import org.snowjak.city.input.ScreenDragEndEvent;
import org.snowjak.city.input.ScreenDragStartEvent;
import org.snowjak.city.input.ScreenDragUpdateEvent;
import org.snowjak.city.input.ScrollEvent;
import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.renderer.MapMode;
import org.snowjak.city.map.renderer.MapRenderer;
import org.snowjak.city.module.ui.ModuleWindow;
import org.snowjak.city.screens.loadingtasks.CompositeLoadingTask;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.LoggerService;
import org.snowjak.city.service.SkinService;
import org.snowjak.city.service.loadingtasks.GameMapEntityDestructionTask;
import org.snowjak.city.service.loadingtasks.GameModulesUninitializationTask;
import org.snowjak.city.tools.Tool;
import org.snowjak.city.tools.ui.Toolbar;
import org.snowjak.city.util.UnregistrationHandle;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
	
	private final LoadingScreen loadingScreen;
	private final MainMenuScreen mainMenuScreen;
	private final I18NService i18nService;
	
	public GameScreen(GameService gameService, Console console, I18NService i18nService, SkinService skinService,
			GameAssetService assetService, Stage stage, LoadingScreen loadingScreen, MainMenuScreen mainMenuScreen) {
		
		super(gameService, console, i18nService, skinService, assetService, stage);
		
		this.i18nService = i18nService;
		this.loadingScreen = loadingScreen;
		this.mainMenuScreen = mainMenuScreen;
		this.renderer = new MapRenderer(gameService.getState());
		
		this.setBackgroundColor(Color.BLACK);
	}
	
	private Window exitConfirmWindow;
	
	private GameInputProcessor inputProcessor;
	private final ScreenViewport viewport = new ScreenViewport();
	{
		viewport.setUnitsPerPixel(1f / MapRenderer.WORLD_GRID_UNIT_SIZE);
	}
	
	private float cameraOffsetX, cameraOffsetY;
	private boolean cameraUpdated = true;
	
	private final MapRenderer renderer;
	float minWorldX, minWorldY, maxWorldX, maxWorldY;
	
	private Rectangle moduleWindowBounds = new Rectangle(0, 0, 0, 0);
	
	private SelectBox<MapMode> mapModeSelectBox;
	private Toolbar buttonList;
	private GameScreenInputHandler inputHandler;
	
	private Set<UnregistrationHandle> inputUnregistrations = new LinkedHashSet<>();
	
	@Initiate(priority = InitPriority.LOWEST_PRIORITY)
	public void init() {
		
		final GameState state = getGameService().getState();
		
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
		
		//
		// Set up the "exit-game" confirmation window.
		final Skin skin = getSkinService().getCurrent();
		
		final TextButton exitConfirmCancelButton = new TextButton(i18nService.get("game-exit-cancel"), skin);
		exitConfirmCancelButton.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				if (exitConfirmCancelButton.isChecked()) {
					exitConfirmCancelButton.setChecked(false);
					exitConfirmWindow.setVisible(false);
				}
			}
		});
		
		final TextButton exitConfirmOkButton = new TextButton(i18nService.get("game-exit-ok"), skin);
		exitConfirmOkButton.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				if (exitConfirmOkButton.isChecked()) {
					exitConfirmOkButton.setChecked(false);
					exitConfirmWindow.setVisible(false);
					loadingScreen.setLoadingTask(
							new CompositeLoadingTask(new GameModulesUninitializationTask(getGameService(), i18nService),
									new GameMapEntityDestructionTask(getGameService(), i18nService)));
					loadingScreen.setLoadingCompleteAction(() -> loadingScreen.changeScreen(mainMenuScreen));
					changeScreen(loadingScreen);
				}
			}
		});
		
		exitConfirmWindow = new Window(i18nService.get("game-exit"), skin);
		exitConfirmWindow.setVisible(false);
		exitConfirmWindow.setModal(true);
		exitConfirmWindow.setMovable(false);
		
		exitConfirmWindow.row().pad(15);
		exitConfirmWindow.add(new Label(i18nService.get("game-exit-text"), skin)).left().colspan(2);
		exitConfirmWindow.row().pad(15);
		exitConfirmWindow.add(exitConfirmCancelButton).right();
		exitConfirmWindow.add(exitConfirmOkButton).left();
		
		exitConfirmWindow.pack();
		
		//
		//
		//
		
		mapModeSelectBox = new SelectBox<MapMode>(skin) {
			
			@Override
			protected String toString(MapMode item) {
				
				return item.getTitle();
			}
		};
		mapModeSelectBox.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				final MapMode previousActiveMode = state.getActiveMapMode();
				if (previousActiveMode != null) {
					previousActiveMode.getTools().forEach(tid -> {
						final Tool t = state.getTools().get(tid);
						if (t != null)
							t.setEnabled(false);
					});
					previousActiveMode.getWindows().forEach(wid -> {
						final ModuleWindow window = state.getWindows().get(wid);
						if (window != null)
							window.removeFromParent();
					});
					
					previousActiveMode.getOnDeactivate().forEach(Runnable::run);
				}
				
				final MapMode newActiveMode = (MapMode) mapModeSelectBox.getSelected();
				if (newActiveMode != null) {
					newActiveMode.getTools().forEach(tid -> {
						final Tool t = state.getTools().get(tid);
						if (t != null)
							t.setEnabled(true);
					});
					
					newActiveMode.getWindows().forEach(wid -> {
						final ModuleWindow window = state.getWindows().get(wid);
						if (window == null)
							return;
						
						window.addTo(getStage());
						window.show();
						window.realign(moduleWindowBounds);
					});
					
					newActiveMode.getOnActivate().forEach(Runnable::run);
				}
				
				state.setActiveMapMode(newActiveMode);
			}
		});
		mapModeSelectBox.setSelected(null);
		
		//
		//
		//
		
		buttonList = new Toolbar(i18nService, getSkinService(), getGameService(), getAssetService(),
				() -> getStage().setScrollFocus(null));
		
		buttonList.setPosition(0, getStage().getHeight(), Align.topLeft);
		
	}
	
	@Override
	public void dispose() {
		
		super.dispose();
		
		renderer.dispose();
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
		
		inputUnregistrations.add(inputProcessor.register(KeyDownEvent.class, e -> {
			if (e.getKeycode() == Input.Keys.ESCAPE)
				if (getGameService().getState().getActiveTool() != null)
					Gdx.app.postRunnable(() -> getGameService().getState().getActiveTool().deactivate());
				else
					exitConfirmWindow.setVisible(true);
		}));
		
		state.getModules().forEach((id, module) -> {
			if (!module.getActivated())
				module.getOnActivationActions().forEach(Runnable::run);
		});
		
		//
		//
		//
		
		//
		// Alphabetize the list of map-modes, but ensure that the "default" map-mode
		// always gets to the top of the list.
		final List<MapMode> alphabetizedMapModes = getGameService().getState().getMapModes().values().stream()
				.sorted((m1, m2) -> {
					if (m1.getId().equals(MapRenderer.DEFAULT_MAP_MODE_ID))
						return -1;
					if (m2.getId().equals(MapRenderer.DEFAULT_MAP_MODE_ID))
						return +1;
					return m1.getTitle().compareTo(m2.getTitle());
				}).collect(Collectors.toList());
		
		mapModeSelectBox.setItems(alphabetizedMapModes.toArray(new MapMode[0]));
		final MapMode activeMapMode = getGameService().getState().getActiveMapMode();
		if (activeMapMode != null)
			mapModeSelectBox.setSelectedIndex(alphabetizedMapModes.indexOf(activeMapMode));
			
		//
		//
		//
		
		getStage().addActor(buttonList);
		getStage().addActor(mapModeSelectBox);
		getStage().addActor(exitConfirmWindow);
	}
	
	@Override
	public void hide() {
		
		super.hide();
		
		buttonList.remove();
		mapModeSelectBox.remove();
		exitConfirmWindow.remove();
		
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
		
		exitConfirmWindow.setPosition(width / 2, height / 2, Align.center);
		mapModeSelectBox.setPosition(width, 0, Align.bottomRight);
		
		moduleWindowBounds = new Rectangle(0, mapModeSelectBox.getHeight(), width,
				height - mapModeSelectBox.getHeight());
		
		final GameState state = getGameService().getState();
		state.getActiveMapMode().getWindows().forEach(wid -> state.getWindows().get(wid).realign(moduleWindowBounds));
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
