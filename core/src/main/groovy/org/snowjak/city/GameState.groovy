/**
 * 
 */
package org.snowjak.city
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

import org.snowjak.city.input.GameInputProcessor
import org.snowjak.city.input.hotkeys.HotkeyRegistry
import org.snowjak.city.map.CityMap
import org.snowjak.city.map.renderer.MapMode
import org.snowjak.city.map.renderer.MapRenderer
import org.snowjak.city.map.renderer.RenderingHookRegistry
import org.snowjak.city.map.renderer.MapRenderer.MapRendererSettings
import org.snowjak.city.module.Module
import org.snowjak.city.module.ModuleExceptionRegistry
import org.snowjak.city.module.ui.ModuleWindow
import org.snowjak.city.screens.GameScreen.GameCameraControl
import org.snowjak.city.service.GameAssetService
import org.snowjak.city.service.I18NService
import org.snowjak.city.tools.Tool
import org.snowjak.city.tools.ui.Toolbar

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.utils.Disposable

import groovy.beans.Bindable

@Bindable
public class GameState {
	
	/**
	 * Seed to be used for random-number generation.
	 */
	String seed = Long.toString(System.currentTimeMillis())
	
	static final Random RND = new Random()
	
	/**
	 * Should the game display its current frames-per-second?
	 */
	boolean showFPS = true
	
	/**
	 * Active {@link CityMap} (may be {@code null})
	 */
	CityMap map
	
	/**
	 * Settings controlling map-rendering. (only referenced during map-renderer initialization)
	 */
	final MapRendererSettings mapRendererSettings = MapRenderer.SETTINGS;
	
	/**
	 * Active camera-controller (may be {@code null})
	 */
	GameCameraControl camera
	
	/**
	 * Active {@link GameInputProcessor} (may be {@code null})
	 */
	GameInputProcessor inputProcessor
	
	/**
	 * Registry/Dispatcher for all active {@link Hotkey}s
	 */
	final HotkeyRegistry hotkeys = new HotkeyRegistry()
	
	/**
	 * All loaded, enabled, and initialized {@link Module}s, by id
	 */
	final Map<String,Module> modules = new LinkedHashMap<>()
	
	/**
	 * {@link Tool}s declared by (initialized) {@link Module}s
	 */
	final Map<String, Tool> tools = new LinkedHashMap<>()
	
	/**
	 * {@link ModuleWindow}s declared by (initialized) {@link Module}s
	 */
	final Map<String,ModuleWindow> windows = new LinkedHashMap<>()
	
	/**
	 * The currently-active {@link Tool} (may be {@code null})
	 */
	Tool activeTool
	
	/**
	 * {@link MapMode}s declared by (initialized) {@link Module}s
	 */
	final Map<String, MapMode> mapModes = new LinkedHashMap<>()
	
	/**
	 * The currently-active {@link MapMode}
	 */
	MapMode activeMapMode
	
	/**
	 * Entity-processing {@link Engine}
	 */
	final Engine engine = new PooledEngine(64, 512, 8, 64)
	
	/**
	 * Endpoint for registering/un-registering your rendering-hooks
	 */
	final RenderingHookRegistry renderingHookRegistry = new RenderingHookRegistry()
	
	/**
	 * Renderer for registered button-activated tools
	 */
	Toolbar toolbar
	
	/**
	 * Registry for Module-execution failures
	 */
	final ModuleExceptionRegistry moduleExceptionRegistry
	
	/**
	 * If you need to create a Disposable resource manually and can't determine when
	 * it should be disposed-of, add it to this set. It will be disposed automatically
	 * when the application exits.
	 */
	final Set<Disposable> disposables = new LinkedHashSet<>()
	
	public GameState(GameAssetService assetService, I18NService i18nService) {
		
		final defaultMapMode = new MapMode(MapRenderer.DEFAULT_MAP_MODE_ID)
		defaultMapMode.title = i18nService.get("mapmode-default-title")
		defaultMapMode.description = i18nService.get("mapmode-default-description")
		mapModes["$defaultMapMode.id"] = defaultMapMode
		
		activeMapMode = defaultMapMode
		
		this.moduleExceptionRegistry = new ModuleExceptionRegistry(assetService)
		
		this.addPropertyChangeListener('seed', { PropertyChangeEvent e ->
			RND.setSeed(seed.hashCode())
		} as PropertyChangeListener)
	}
}