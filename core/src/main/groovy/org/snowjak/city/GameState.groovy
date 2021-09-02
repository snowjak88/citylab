/**
 * 
 */
package org.snowjak.city

import org.snowjak.city.input.GameInputProcessor
import org.snowjak.city.input.hotkeys.HotkeyRegistry
import org.snowjak.city.map.CityMap
import org.snowjak.city.map.renderer.MapRenderer
import org.snowjak.city.screens.GameScreen.GameCameraControl
import org.snowjak.city.tools.Tool
import org.snowjak.city.tools.ui.ToolButtonList

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine

public class GameState {
	
	/**
	 * Seed to be used for random-number generation.
	 */
	String seed = Long.toString(System.currentTimeMillis())
	
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
	 * The main world renderer
	 */
	final MapRenderer renderer = new MapRenderer()
	
	/**
	 * Renderer for registered button-activated tools
	 */
	ToolButtonList buttonRenderer
}