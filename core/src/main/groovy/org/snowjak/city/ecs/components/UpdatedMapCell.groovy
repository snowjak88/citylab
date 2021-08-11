/**
 * 
 */
package org.snowjak.city.ecs.components

import com.badlogic.ashley.core.Component

/**
 * Indicates that an entity that is {@link AtMapCell} has had its map-cell coordinates updated. The {@link CityMap} instance should be updated to point to this Entity from the correct cell.
 * @author snowjak88
 *
 */
class UpdatedMapCell implements Component {
	float cellX, cellY
}
