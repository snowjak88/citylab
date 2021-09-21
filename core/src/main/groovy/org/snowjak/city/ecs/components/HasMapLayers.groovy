package org.snowjak.city.ecs.components

import org.snowjak.city.map.tiles.Tile

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.IntMap
import com.badlogic.gdx.utils.Pool.Poolable

/**
 * Indicates that a map-cell has one or more assigned layers -- String IDs
 * put into some order, each of which can be associated with a {@link Tile}.
 * 
 * @author snowjak88
 *
 */
class HasMapLayers implements Component, Poolable {
	
	final Map<String, Tile> tiles = [:]
	final Map<String, Color> tints = [:]
	final Map<String,Integer> altitudeOverrides = [:]
	
	public void removeAll(String layerID) {
		tiles.remove layerID
		tints.remove layerID
		altitudeOverrides.remove layerID
	}
	
	public void reset() {
		tiles.clear()
		tints.clear()
		altitudeOverrides.clear()
	}
}
