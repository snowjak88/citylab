package org.snowjak.city.ecs.systems.impl

import org.snowjak.city.ecs.components.IsMapCell
import org.snowjak.city.ecs.components.IsNonBuildableCell
import org.snowjak.city.ecs.systems.WindowIteratingSystem

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family

/**
 * Periodically, check cells marked as NonBuildable to see if their list of
 * blocker-IDs is empty. If so, remove their NonBuildable status.
 * 
 * @author snowjak88
 *
 */
class MapCellBlockerRemovingSystem extends WindowIteratingSystem {
	
	private final ComponentMapper<IsNonBuildableCell> nonBuildableMapper = ComponentMapper.getFor(IsNonBuildableCell)
	
	public MapCellBlockerRemovingSystem() {
		super(8, Family.all(IsNonBuildableCell, IsMapCell).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final nb = nonBuildableMapper.get(entity)
		if(nb.blockerIDs.isEmpty())
			entity.remove IsNonBuildableCell
	}
}
