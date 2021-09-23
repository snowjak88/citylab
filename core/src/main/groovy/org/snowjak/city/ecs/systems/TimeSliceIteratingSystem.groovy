package org.snowjak.city.ecs.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.utils.TimeUtils

/**
 * IteratingSystem in which each cycle's execution is limited to a certain time-slice.
 * <p>
 * c.f. {@link WindowIteratingSystem} in which each cycle's execution is limited to a certain number of Entities.
 * </p>
 * 
 * @author snowjak88
 *
 */
abstract class TimeSliceIteratingSystem extends EntitySystem implements EntityListener {
	
	private final Family family
	private final long delta
	
	private final LinkedList<Entity> entities = new LinkedList<>()
	
	/**
	 * Construct a new TimeLimitedIteratingSystem, operating on the given {@link Family},
	 * with each cycle taking no longer than {@code delta} seconds.
	 * @param family
	 * @param delta
	 */
	public TimeSliceIteratingSystem(Family family, float delta) {
		
		this(family, delta, 0)
	}
	
	/**
	 * Construct a new TimeLimitedIteratingSystem, operating on the given {@link Family},
	 * with each cycle taking no longer than {@code delta} seconds.
	 * @param family
	 * @param delta
	 * @param priority
	 */
	public TimeSliceIteratingSystem(Family family, float delta, int priority) {
		
		super(priority);
		this.family = family
		this.delta = TimeUtils.millisToNanos((long)(delta * 1000f))
	}
	
	@Override
	public void update(float deltaTime) {
		
		def n = 0
		def currentDelta = 0l
		while(currentDelta < delta && n < entities.size()) {
			def startTime = System.nanoTime()
			
			final e = entities.poll()
			if(e) {
				processEntity e, deltaTime
				entities.addLast e
			}
			
			n++
			currentDelta += (System.nanoTime() - startTime)
		}
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
	
	@Override
	public void entityAdded(Entity entity) {
		
		entities << entity
	}
	@Override
	public void entityRemoved(Entity entity) {
		
		entities.remove entity
	}
	@Override
	public void addedToEngine(Engine engine) {
		
		engine.addEntityListener family, this
	}
	@Override
	public void removedFromEngine(Engine engine) {
		
		engine.removeEntityListener this
	}
}
