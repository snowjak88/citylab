/**
 * 
 */
package org.snowjak.city.ecs.systems.impl;

import org.snowjak.city.GameState;
import org.snowjak.city.ecs.components.IsMapVertex;
import org.snowjak.city.ecs.systems.ListeningSystem;
import org.snowjak.city.map.CityMap;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;

/**
 * When an Entity receives an {@link IsMapVertex} component, this system handles
 * sticking it into the Map at the correct space.
 * <p>
 * <strong>Note</strong> that this system is fairly dumb -- the moment you add
 * {@link IsMapVertex} to an entity, this system will read that component's
 * assigned {@link IsMapVertex#getVertexX() vertexX} and {@link IsMapVertex#getVertexY()
 * vertexY} coordinates, and associate that Entity with that location in the
 * {@link CityMap}.
 * </p>
 * <p>
 * Make sure you assign vertexX and vertexY <strong>before</strong> you add the
 * component to the Entity.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class IsMapVertexManagementSystem extends ListeningSystem {
	
	private final ComponentMapper<IsMapVertex> isVertexMapper = ComponentMapper.getFor(IsMapVertex.class);
	
	private final GameState gameState;
	
	public IsMapVertexManagementSystem(GameState gameState) {
		
		super(Family.all(IsMapVertex.class).get(), -1);
		
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
		
		final IsMapVertex vertex = isVertexMapper.get(entity);
		
		map.setVertexEntity((int) vertex.getVertexX(), (int) vertex.getVertexY(), entity);
	}
	
	@Override
	public void dropped(Entity entity, float deltaTime) {
		
		//
		// nothing to do here, really ...
	}
}
