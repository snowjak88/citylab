/**
 * 
 */
package org.snowjak.city.ecs.systems;

import org.snowjak.city.GameData;
import org.snowjak.city.ecs.components.IsMapCell;
import org.snowjak.city.map.CityMap;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;

/**
 * When an Entity receives an {@link IsMapCell} component, this system handles
 * sticking it into the Map at the correct space.
 * 
 * @author snowjak88
 *
 */
public class IsMapCellManagementSystem extends ListeningSystem {
	
	private final ComponentMapper<IsMapCell> isCellMapper = ComponentMapper.getFor(IsMapCell.class);
	
	public IsMapCellManagementSystem() {
		
		super(Family.all(IsMapCell.class).get(), -1);
	}
	
	@Override
	public boolean checkProcessing() {
		
		if (GameData.get().map == null)
			return false;
		
		return super.checkProcessing();
	}
	
	@Override
	public void added(Entity entity, float deltaTime) {
		
		final CityMap map = GameData.get().map;
		
		final IsMapCell cell = isCellMapper.get(entity);
		
		map.addEntity((int) cell.getCellX(), (int) cell.getCellY(), entity);
	}
	
	@Override
	public void dropped(Entity entity, float deltaTime) {
		
		final CityMap map = GameData.get().map;
		
		final IsMapCell cell = isCellMapper.get(entity);
		
		map.removeEntity((int) cell.getCellX(), (int) cell.getCellY(), entity);
	}
}
