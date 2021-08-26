package org.snowjak.city.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

/**
 * @author snowjak88
 *
 */
public class IsMapCell implements Component, Poolable {
	float cellX, cellY

	@Override
	public void reset() {
		
		cellX = 0
		cellY = 0
	}
}
