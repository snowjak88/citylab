package network

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool.Poolable

abstract class IsNetworkNode implements Poolable {
	
	final Set<Entity> connections = []
	float cost = 1f
	
	void reset() {
		connections.clear()
		cost = 1f
	}
}