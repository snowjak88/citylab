package org.snowjak.city.map.renderer

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
	 * The set of {@link Tool#getId() tool IDs} available in this map-mode
	 */
	final Set<String> tools = []
	
	public MapMode(String id) {
		this.id = id
	}
}
