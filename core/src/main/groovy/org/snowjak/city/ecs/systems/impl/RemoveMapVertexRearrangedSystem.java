/**
 * 
 */
package org.snowjak.city.ecs.systems.impl;

import org.snowjak.city.ecs.components.IsMapVertex;
import org.snowjak.city.ecs.components.IsMapVertexRearranged;
import org.snowjak.city.ecs.systems.EventComponentSystem;
import org.snowjak.city.ecs.systems.ListeningSystem;

/**
 * Wherever we have an {@link IsMapVertex} and {@link IsMapVertexRearranged} --
 * this removes the {@link IsMapVertexRearranged}, as it is assumed that all
 * necessary {@link ListeningSystem}s have already reacted to it.
 * 
 * @author snowjak88
 *
 */
public class RemoveMapVertexRearrangedSystem extends EventComponentSystem {
	
	public RemoveMapVertexRearrangedSystem() {
		
		super(IsMapVertexRearranged.class);
	}
}
