package org.snowjak.city.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

/**
 * An "event-Component" that exists purely to notify the
 * {@link org.snowjak.city.ecs.systems.impl.UnselectionSystem}
 * to remove {@link IsSelected} from all Entities.
 * 
 * @author snowjak88
 *
 */
class UnselectAll implements Component, Poolable {
	void reset() { }
}
