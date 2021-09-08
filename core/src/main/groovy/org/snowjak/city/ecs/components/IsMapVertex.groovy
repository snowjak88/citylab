package org.snowjak.city.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

/**
 * @author snowjak88
 *
 */
public class IsMapVertex implements Component, Poolable {
	float vertexX, vertexY

	@Override
	public void reset() {
		
		vertexX = 0
		vertexY = 0
	}
}
