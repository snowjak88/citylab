import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

import org.snowjak.city.map.tiles.TileEdge

class IsNetworkNode implements Component, Poolable {
	final Set<TileEdge> edges = []
	void reset() {
		edges.clear()
	}
}