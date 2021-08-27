/**
 * 
 */
package org.snowjak.city.ecs.systems.impl;

import org.snowjak.city.ecs.components.IsMapCell;
import org.snowjak.city.ecs.components.IsMapCellRearranged;
import org.snowjak.city.ecs.systems.ListeningSystem;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * Wherever we have an {@link IsMapCell} and {@link IsMapCellRearranged} -- this
 * removes the {@link IsMapCellRearranged}, as it is assumed that all necessary
 * {@link ListeningSystem}s have already reacted to it.
 * 
 * @author snowjak88
 *
 */
public class RemoveMapCellRearrangedSystem extends IteratingSystem {
	
	public RemoveMapCellRearrangedSystem() {
		
		super(Family.all(IsMapCell.class, IsMapCellRearranged.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		entity.remove(IsMapCellRearranged.class);
	}
}
