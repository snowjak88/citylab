package org.snowjak.city.ecs

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.Family

class GatheringEntityListener implements EntityListener, Iterable<Entity> {

	final Set<Entity> entities = []
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
