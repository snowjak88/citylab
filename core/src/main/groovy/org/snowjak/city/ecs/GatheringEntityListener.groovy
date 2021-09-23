package org.snowjak.city.ecs

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.Family

/**
 * An implementation of {@link EntityListener} that simply aggregates all appropriate Entities into a set, {@link #entities}.
 * @author snowjak88
 *
 */
class GatheringEntityListener implements EntityListener, Iterable<Entity> {
	
	/**
	 * The current set of Entities matching {@link #family}.
	 */
	final Set<Entity> entities = []
	
	/**
	 * The configured {@link Family} for this EntityListener.
	 */
	final Family family
	
	public GatheringEntityListener(Family family) {
		this.family = family
	}
	
	@Override
	public void entityAdded(Entity entity) {
		
		entities << entity
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		
		entities.remove entity
	}
	
	@Override
	public Iterator<Entity> iterator() {
		
		entities.iterator()
	}
}
