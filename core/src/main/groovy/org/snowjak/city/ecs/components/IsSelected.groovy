package org.snowjak.city.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

/**
 * Denotes that an entity is "selected" somehow.
 * Implementations may vary in how they handle this, if at all.
 * 
 * @author snowjak88
 *
 */
class IsSelected implements Component, Poolable {
	
	Status status = Status.DEFAULT
	
	void reset() {
		status = Status.DEFAULT
	}
	
	public enum Status {
		DEFAULT, INVALID, WARNING, INFORMATION, OTHER
	}
}
