package org.snowjak.city.ecs.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IntervalSystem


/**
 * A variant of {@link IntervalSystem}. Every {@code interval} (measured in seconds), executes
 * {@link #processEntity(Entity, float) processEntity()} for every Entity in the configured
 * {@link Family}.
 * 
 * @author snowjak88
 *
 */
abstract class IntervalIteratingSystem extends IntervalSystem implements EntityListener {
	
	private final Family family
	
	private final Set<Entity> entities = new LinkedHashSet<>()
	
	/**
	 * Construct a new IntervalIteratingSystem that will call
	 * {@link #processEntity(Entity, float) processEntity()} for all Entities
	 * in the given {@code family} every {@code interval} seconds.
	 * @param family
	 * @param interval
	 * @param priority
	 */
	public IntervalIteratingSystem(Family family, float interval, int priority) {
		
		super(interval, priority)
		this.family = family
	}
	
	/**
	 * Construct a new IntervalIteratingSystem that will call
	 * {@link #processEntity(Entity, float) processEntity()} for all Entities
	 * in the given {@code family} every {@code interval} seconds.
	 * @param family
	 * @param interval
	 */
	public IntervalIteratingSystem(Family family, float interval) {
		
		super(interval)
		this.family = family
	}
	
	@Override
	protected void updateInterval() {
		
		entities.each { processEntity it, interval }
	}
	
	
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine)
		
		engine.addEntityListener family, this
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		super.removedFromEngine(engine);
		
		engine.removeEntityListener this
	}
	
	@Override
	public void entityAdded(Entity entity) {
		
		entities << entity
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		
		entities.remove entity
	}
	
	/**
	 * This method is called on every entity on every update call of the
	 * EntitySystem. Override this to implement your system's specific processing.
	 *
	 * @param entity
	 *            The current Entity being processed
	 * @param deltaTime
	 *            The delta time between the last and current frame
	 */
	protected abstract void processEntity(Entity entity, float deltaTime);
}
