/**
 * 
 */
package org.snowjak.city.ecs.systems.impl;

import org.snowjak.city.GameState;
import org.snowjak.city.ecs.components.IsMapCell;
import org.snowjak.city.ecs.systems.ListeningSystem;
import org.snowjak.city.map.CityMap;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;

/**
 * When an Entity receives an {@link IsMapCell} component, this system handles
 * sticking it into the Map at the correct space.
 * <p>
 * <strong>Note</strong> that this system is fairly dumb -- the moment you add
 * {@link IsMapCell} to an entity, this system will read that component's
 * assigned {@link IsMapCell#getCellX() cellX} and {@link IsMapCell#getCellY()
 * cellY} coordinates, and associate that Entity with that location in the
 * {@link CityMap}.
 * </p>
 * <p>
 * Make sure you assign cellX and cellY <strong>before</strong> you add the
 * component to the Entity.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class IsMapCellManagementSystem extends ListeningSystem {
	
	private final ComponentMapper<IsMapCell> isCellMapper = ComponentMapper.getFor(IsMapCell.class);
	
	private final GameState gameState;
	
	public IsMapCellManagementSystem(GameState gameState) {
		
		super(Family.all(IsMapCell.class).get(), -1);
		
		this.gameState = gameState;
	}
	
	@Override
	public boolean checkProcessing() {
		
		if (gameState.getMap() == null)
			return false;
		
		return super.checkProcessing();
	}
	
	@Override
	public void added(Entity entity, float deltaTime) {
		
		final CityMap map = gameState.getMap();
		
		final IsMapCell cell = isCellMapper.get(entity);
		
		map.setEntity((int) cell.getCellX(), (int) cell.getCellY(), entity);
	}
	
	@Override
	public void dropped(Entity entity, float deltaTime) {
		
		final CityMap map = gameState.getMap();
		
		final IsMapCell cell = isCellMapper.get(entity);
		
		map.setEntity((int) cell.getCellX(), (int) cell.getCellY(), null);
	}
}
