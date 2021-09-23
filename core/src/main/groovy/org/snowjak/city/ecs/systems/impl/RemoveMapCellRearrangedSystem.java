/**
 * 
 */
package org.snowjak.city.ecs.systems.impl;

import org.snowjak.city.ecs.components.IsMapCell;
import org.snowjak.city.ecs.components.IsMapCellRearranged;
import org.snowjak.city.ecs.systems.EventComponentSystem;
import org.snowjak.city.ecs.systems.ListeningSystem;

/**
 * Wherever we have an {@link IsMapCell} and {@link IsMapCellRearranged} -- this
 * removes the {@link IsMapCellRearranged}, as it is assumed that all necessary
 * {@link ListeningSystem}s have already reacted to it.
 * 
 * @author snowjak88
 *
 */
public class RemoveMapCellRearrangedSystem extends EventComponentSystem {
	
	public RemoveMapCellRearrangedSystem() {
		
		super(IsMapCellRearranged.class);
	}
}
