package network

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool.Poolable

abstract class IsNetworkNode implements Poolable {
	
	final Set<Entity> connections = []
	String networkID
	float cost = 1f
	
	void reset() {
		connections.clear()
		networkID = null
		cost = 1f
	}
}