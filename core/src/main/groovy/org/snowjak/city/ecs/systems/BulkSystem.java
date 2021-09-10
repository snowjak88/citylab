package org.snowjak.city.ecs.systems;

import java.util.LinkedHashSet;
import java.util.Set;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * {@link EntitySystem} that operates on all relevant {@link Entity Entities} in
 * bulk. (Compare with {@link IteratingSystem} and its ilk that operate on
 * Entities one-by-one.)
 * <p>
 * Every cycle, this system will call its {@link #update(Set, float)}, with all
 * relevant Entities in the given Set.
 * </p>
 * 
 * @author snowjak88
 *
 */
public abstract class BulkSystem extends EntitySystem implements EntityListener {
	
	private final LinkedHashSet<Entity> entities = new LinkedHashSet<>();
	private final Family family;
	
	/**
	 * Construct a new BulkSystem, operating on the given {@link Family} with
	 * priority 0.
	 * 
	 * @param family
	 */
	public BulkSystem(Family family) {
		
		this(family, 0);
	}
	
	/**
	 * Construct a new BulkSystem, operating on the given {@link Family} with the
	 * given {@code priority}.
	 * 
	 * @param family
	 * @param priority
	 */
	public BulkSystem(Family family, int priority) {
		
		super(priority);
		this.family = family;
	}
	
	@Override
	public void entityAdded(Entity entity) {
		
		entities.add(entity);
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		
		entities.remove(entity);
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		engine.addEntityListener(family, this);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		engine.removeEntityListener(this);
	}
	
	@Override
	public void update(float deltaTime) {
		
		update(entities, deltaTime);
	}
	
	/**
	 * This method is called on all entities on every update call of the
	 * EntitySystem. Override this to implement your system's specific processing.
	 *
	 * @param entities
	 *            The current set of Entities being processed
	 * @param deltaTime
	 *            The delta time between the last and current frame
	 */
	protected abstract void update(Set<Entity> entities, float deltaTime);
}
