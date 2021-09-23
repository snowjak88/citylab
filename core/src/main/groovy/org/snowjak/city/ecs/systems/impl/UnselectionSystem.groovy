package org.snowjak.city.ecs.systems.impl

import org.snowjak.city.ecs.GatheringEntityListener
import org.snowjak.city.ecs.components.IsSelected
import org.snowjak.city.ecs.components.UnselectAll
import org.snowjak.city.ecs.systems.ListeningSystem

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.Family

/**
 * Whenever any Entity is tagged with {@link UnselectAll}, this System
 * will remove {@link IsSelected} from all Entities.
 * 
 * @author snowjak88
 *
 */
class UnselectionSystem extends ListeningSystem {
	
	private final GatheringEntityListener selectedEntities
	
	public UnselectionSystem() {
		
		super(Family.all(UnselectAll).get());
		selectedEntities = new GatheringEntityListener(Family.all(IsSelected).get())
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		engine.addEntityListener selectedEntities.family, selectedEntities
	}
	
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		engine.removeEntityListener selectedEntities
	}
	
	@Override
	protected void added(Entity entity, float deltaTime) {
		
		selectedEntities.each { it.remove IsSelected }
	}
	
	@Override
	protected void dropped(Entity entity, float deltaTime) {
		
		//
		// nothing to do
	}
}
