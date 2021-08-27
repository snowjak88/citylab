/**
 * 
 */
package org.snowjak.city.ecs.systems;

import java.util.LinkedList;

import org.snowjak.city.util.Util;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * A variant of {@link IteratingSystem} that only processes at most
 * <strong>N</strong> Entities every cycle.
 * 
 * @author snowjak88
 *
 */
public abstract class WindowIteratingSystem extends EntitySystem implements EntityListener {
	
	private final int n;
	private final Family family;
	private final LinkedList<Entity> entities = new LinkedList<>();
	
	/**
	 * Construct a new WindowIteratingSystem that operates on, at most, {@code n}
	 * Entities within the given {@link Family}, at a priority of 0.
	 * 
	 * @param n
	 * @param family
	 */
	public WindowIteratingSystem(int n, Family family) {
		
		this(n, family, 0);
	}
	
	/**
	 * Construct a new WindowIteratingSystem that operates on, at most, {@ccode n}
	 * Entities within the given Family, with the configured priority.
	 * 
	 * @param n
	 * @param family
	 * @param priority
	 *            lower value = higher priority
	 */
	public WindowIteratingSystem(int n, Family family, int priority) {
		
		super(priority);
		
		this.n = n;
		this.family = family;
	}
	
	@Override
	public void update(float deltaTime) {
		
		final int maxI = Util.min(entities.size(), n);
		
		for (int i = 0; i < maxI; i++) {
			final Entity current = entities.removeFirst();
			processEntity(current, deltaTime);
			entities.addLast(current);
		}
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine);
		
		engine.addEntityListener(family, this);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		super.removedFromEngine(engine);
		
		engine.removeEntityListener(this);
	}
	
	@Override
	public void entityAdded(Entity entity) {
		
		entities.add(entity);
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		
		entities.remove(entity);
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
