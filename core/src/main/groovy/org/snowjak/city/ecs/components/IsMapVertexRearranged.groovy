/**
 * 
 */
package org.snowjak.city.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

/**
 * Signifies that a map-vertex has been rearranged somehow.
 * <p>
 * This Component is only retained for <strong>1</strong> cycle. If your functionality is "located on"
 * a cell -- and therefore is most likely sensitive to map-vertex rearrangement -- you should configure
 * a {@link org.snowjak.city.ecs.systems.ListeningSystem} to detect such "add" transitions, and
 * reconfigure your Components to react accordingly.
 * </p>
 * @author snowjak88
 *
 */
class IsMapVertexRearranged implements Component, Poolable {
	
	@Override
	public void reset() {
	}
}
