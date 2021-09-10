/**
 * 
 */
package org.snowjak.city.ecs.systems.impl;

import org.snowjak.city.ecs.components.IsMapVertex;
import org.snowjak.city.ecs.components.IsMapVertexRearranged;
import org.snowjak.city.ecs.systems.ListeningSystem;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * Wherever we have an {@link IsMapVertex} and {@link IsMapVertexRearranged} -- this
 * removes the {@link IsMapVertexRearranged}, as it is assumed that all necessary
 * {@link ListeningSystem}s have already reacted to it.
 * 
 * @author snowjak88
 *
 */
public class RemoveMapVertexRearrangedSystem extends IteratingSystem {
	
	public RemoveMapVertexRearrangedSystem() {
		
		super(Family.all(IsMapVertex.class, IsMapVertexRearranged.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		entity.remove(IsMapVertexRearranged.class);
	}
}
