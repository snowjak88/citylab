package archetypes

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentType
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity

class Archetype {
	String id, title = '(?)', description = '(?)'
	
	/**
	 * Set of Component-types to configure, along with their corresponding "instantializers".
	 * <p>
	 * An "initializer" is simply a Closure of the form <code>{ entity, component -> ... }</code>.
	 * An initializer of {@code null} signifies "no initialization required".
	 * </p>
	 * <p>
	 * When an archetype is instantiated, its corresponding component-types are created
	 * (if they do not already exist) for the target entity. The component-type's corresponding
	 * initializer is then called, receiving as its two parameters the entity being instantiated,
	 * and the given component-instance.
	 * </p>
	 */
	Map<Class<? extends Component>, Closure> initializers = [:]
	
	/**
	 * Copy all initializers defined in the given Archetype into this Archetype.
	 * Any Component definition that already exists on this Archetype will be overwritten.
	 * 
	 * @param inheritee
	 */
	public void inherits(Archetype inheritee) {
		this.initializers.putAll inheritee.initializers
	}
	
	/**
	 * Configure the given Entity to be an instance of this Archetype.
	 * 
	 * @param entity
	 * @param engine
	 */
	public void instantiate(Entity entity, Engine engine) {
		
		initializers.each { type, initializer ->
			
			if(!type)
				return
			
			def component = entity.getComponent( type )
			if(!component)
				component = entity.addAndReturn( engine.createComponent( type ) )
			
			if(initializer)
				initializer(entity, component)
		}
	}
}