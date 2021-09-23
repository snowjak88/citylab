package org.snowjak.city.ecs.systems.impl

import org.snowjak.city.ecs.GatheringEntityListener
import org.snowjak.city.ecs.components.IsSelected
import org.snowjak.city.ecs.components.UnselectAll
import org.snowjak.city.ecs.systems.EventComponentSystem

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family

class UnselectAllEventSystem extends EventComponentSystem {
	
	private final GatheringEntityListener selectedEntities;
	
	public UnselectAllEventSystem() {
		
		super(UnselectAll)
		
		selectedEntities = new GatheringEntityListener(Family.all(IsSelected).get())
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine)
		engine.addEntityListener selectedEntities.family, selectedEntities
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine)
		engine.removeEntityListener selectedEntities
	}
	
	@Override
	protected void onEvent(Entity entity, float deltaTime) {
		selectedEntities.each { it.remove IsSelected }
	}
}
