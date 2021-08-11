/**
 * 
 */
package org.snowjak.city.ecs.systems;

import org.snowjak.city.GameData;
import org.snowjak.city.ecs.components.AtMapCell;
import org.snowjak.city.ecs.components.UpdatedMapCell;
import org.snowjak.city.map.CityMap;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * @author snowjak88
 *
 */
public class MapCellUpdatingSystem extends IteratingSystem {
	
	public MapCellUpdatingSystem() {
		
		super(Family.all(UpdatedMapCell.class).get(), -1);
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		AtMapCell mapCell = entity.getComponent(AtMapCell.class);
		
		final UpdatedMapCell updatedCell = entity.getComponent(UpdatedMapCell.class);
		
		final CityMap map = GameData.get().map;
		if (map == null)
			return;
		
		if (mapCell != null)
			map.removeEntity((int) mapCell.getCellX(), (int) mapCell.getCellY(), entity);
		
		map.addEntity((int) updatedCell.getCellX(), (int) updatedCell.getCellY(), entity);
		
		if (mapCell == null)
			mapCell = (AtMapCell) entity.addAndReturn(new AtMapCell());
		
		mapCell.setCellX(updatedCell.getCellX());
		mapCell.setCellY(updatedCell.getCellY());
		
		entity.remove(UpdatedMapCell.class);
	}
}
