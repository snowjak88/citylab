package org.snowjak.city.ecs.components

import org.snowjak.city.map.tiles.Tile

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

class HasMapCellTiles implements Component, Poolable {
	
	final List<MapCellTile> tiles = []
	
	void reset() {
		tiles.clear()
	}
	
	public static class MapCellTile {
		
		Tile tile
		Integer altitudeOverride
		
	}
}
