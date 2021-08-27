package org.snowjak.city.ecs.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family

/**
 * An {@link EntitySystem} that listens for Entities receiving Components. An
 * Entity is processed through this system only when it satisfies this system's
 * {@link Family} -- and then, only once.
 * 
 * @author snowjak88
 *
 */
abstract class ListeningSystem extends EntitySystem implements EntityListener {
	
	private final Family family
	private final Set<Entity> entities = new HashSet<>()
	
	private final Queue<Entity> newlyAdded = new LinkedList<>(), newlyDropped = new LinkedList<>()
	
	public ListeningSystem(Family family, int priority = 0) {
		super(priority)
		
		this.family = family
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine)
		
		engine.addEntityListener family, this
	}
	
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		super.removedFromEngine(engine)
		
		engine.removeEntityListener this
	}
	
	
	@Override
	public void update(float deltaTime) {
		
		super.update(deltaTime)
		
		Entity current
		while(current = newlyAdded.poll())
			added current, deltaTime
		
		current = null
		while(current = newlyDropped.poll())
			dropped current, deltaTime
	}
	
	/**
	 * Called when an {@link Entity} matches this system's configured {@link Family}.
	 * This Entity will not be passed to this method again unless it first gets passed to
	 * {@link #dropped(Entity) dropped()}.
	 * @param entity
	 */
	protected abstract void added(Entity entity, float deltaTime)
	
	/**
	 * Called when an {@link Entity} falls out of this system's configured {@link Family}.
	 * This Entity will not be passed to this method again unless it first gets passed to
	 * {@link #added(Entity) added()}.
	 * @param entity
	 */
	protected abstract void dropped(Entity entity, float deltaTime)
	
	@Override
	public void entityAdded(Entity entity) {
		
		newlyAdded << entity
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		
		newlyDropped << entity
	}
}
