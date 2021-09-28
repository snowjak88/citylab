package org.snowjak.city.map.renderer

import org.snowjak.city.module.ui.ModuleWindow
import org.snowjak.city.tools.Tool
import org.snowjak.city.map.renderer.hooks.AbstractRenderingHook
import org.snowjak.city.util.RelativePriorityList

/**
 * A map-mode specifies what parts of the game-map will be displayed.
 * <p>
 * The {@code "default"} MapMode is defined as that in which the assigned map-tiles are drawn.
 * </p> 
 * 
 * @author snowjak88
 *
 */
class MapMode {
	
	final String id
	String title, description
	
	/**
	 * An ordered list of Map-Mode IDs. While this Map-Mode is active,
	 * these given Map-Modes will all be rendered prior to this Map-Mode
	 * being rendered.
	 */
	final List<String> includes = []
	
	/**
	 * The set of {@link AbstractRenderingHook#getId() rendering-hook-IDs} to be included
	 */
	final Set<String> renderingHooks = []
	
	/**
	 * The set of {@link Tool#id tool IDs} available in this map-mode
	 */
	final Set<String> tools = []
	
	/**
	 * The set of {@link ModuleWindow#id window IDs} to be shown when in this map-mode
	 */
	final Set<String> windows = []
	
	/**
	 * The set of {@link Runnable}s that will be executed when this map-mode is activated
	 */
	final Set<Runnable> onActivate = []
	
	/**
	 * The set of {@link Runnable}s that will be executed when this map-mode is deactivated
	 */
	final Set<Runnable> onDeactivate = []
	
	public MapMode(String id) {
		this.id = id
	}
}
