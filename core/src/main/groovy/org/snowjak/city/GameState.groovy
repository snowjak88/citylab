/**
 * 
 */
package org.snowjak.city
import org.snowjak.city.module.Module
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

import org.snowjak.city.input.GameInputProcessor
import org.snowjak.city.input.hotkeys.HotkeyRegistry
import org.snowjak.city.map.CityMap
import org.snowjak.city.map.renderer.RenderingHookRegistry
import org.snowjak.city.module.ModuleExceptionRegistry
import org.snowjak.city.screens.GameScreen.GameCameraControl
import org.snowjak.city.service.GameAssetService
import org.snowjak.city.tools.Tool
import org.snowjak.city.tools.ui.Toolbar

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine

import groovy.beans.Bindable

@Bindable
public class GameState {
	
	/**
	 * Seed to be used for random-number generation.
	 */
	String seed = Long.toString(System.currentTimeMillis())
	
	final Random rnd = new Random()
	
	/**
	 * Active {@link CityMap} (may be {@code null})
	 */
	CityMap map
	
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
	 * All loaded {@link Module}s, by id
	 */
	final Map<String,Module> modules = new LinkedHashMap<>()
	
	/**
	 * {@link Tool}s declared by (initialized) {@link Module}s
	 */
	final Map<String, Tool> tools = new LinkedHashMap<>()
	
	/**
	 * The currently-active {@link Tool} (may be {@code null})
	 */
	Tool activeTool
	
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
	
	public GameState(GameAssetService assetService) {
		this.moduleExceptionRegistry = new ModuleExceptionRegistry(assetService)
		
		this.addPropertyChangeListener('seed', { PropertyChangeEvent e ->
			rnd.setSeed(seed.hashCode())
		} as PropertyChangeListener)
	}
}