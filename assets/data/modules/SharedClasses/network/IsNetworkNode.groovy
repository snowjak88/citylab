package network

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Pool.Poolable

import org.snowjak.city.map.tiles.TileEdge

class IsNetworkNode implements Component, Poolable {
	
	final Set<Entity> connections = []
	
	void reset() {
		connections.clear()
	}
}