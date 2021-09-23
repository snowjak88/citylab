package org.snowjak.city.ecs.systems

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family

/**
 * An EntitySystem that handles "event-Components" -- i.e., Components that are mere markers that should
 * only persist on an entity for a single update-cycle.
 * <p>
 * You may override {@link #onEvent(Entity,float) onEvent()} with your own functionality, if desired.
 * </p>
 * 
 * @author snowjak88
 *
 */
abstract class EventComponentSystem extends EntitySystem implements EntityListener {
	
	private final Class<? extends Component> eventType
	private final Family family
	
	private final LinkedHashSet<Entity> entitiesReceived = new LinkedHashSet<>(), entitiesToRemove = new LinkedHashSet<>()
	
	public EventComponentSystem(Class<? extends Component> eventType, int priority = 0) {
		super(priority)
		this.eventType = eventType
		this.family = Family.all(eventType).get()
	}
	
	@Override
	public void update(float deltaTime) {
		
		//
		// First, handle all these "entities-to-remove"
		final removeIterator = entitiesToRemove.iterator()
		while(removeIterator.hasNext()) {
			final entity = removeIterator.next()
			onEvent entity, deltaTime
			entity.remove eventType
			removeIterator.remove()
		}
		
		//
		// Now -- move all newly-received entities into the "to-remove" camp
		final addIterator = entitiesReceived.iterator()
		while(addIterator.hasNext()) {
			entitiesToRemove << addIterator.next()
			addIterator.remove()
		}
	}
	
	/**
	 * Called for any Entity holding an instance of this event-component type.
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 * 
	 * @param entity
	 * @param deltaTime
	 */
	protected void onEvent(Entity entity, float deltaTime) { }
	
	@Override
	public void entityAdded(Entity entity) {
		
		entitiesReceived << entity
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		
		//
		// nothing to do
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
