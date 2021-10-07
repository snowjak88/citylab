package network

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool.Poolable

class NetworkPathfinderRequest implements Component, Poolable {
	
	//
	// Has the request been picked up by the Network pathfinder?
	boolean started
	//
	// Has the pathfinder completed its search?
	boolean done
	//
	// Was the pathfinder-search successful?
	boolean success
	
	//
	// The starting and ending Entities
	Entity start, end
	
	//
	// A Closure that determines whether a given connection between two Entities
	// is "valid" for the purposes of this pathfinding-search.
	//
	// Must be of the form { Entity from, Entity to -> ... }
	Closure filter
	
	//
	// If a path is found, this List will be populated with the path's constituent elements
	final List<Entity> path = []
	
	void reset() {
		started = false
		done = false
		success = false
		start = null
		end = null
		filter = null
		path.clear()
	}
}