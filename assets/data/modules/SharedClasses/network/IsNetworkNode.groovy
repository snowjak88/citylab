package network

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool.Poolable

abstract class IsNetworkNode implements Poolable {
	
	final Set<Entity> connections = []
	
	void reset() {
		connections.clear()
	}
}