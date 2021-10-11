id = 'archetypes'

i18n.addBundle 'i18n'

title = i18n.get 'title'
description = i18n.get 'description'

//
//
//

archetypes = new LinkedHashMap<String, Archetype>()
provides archetypes named 'archetypes'

//
//
//

registerArchetype = { obj ->
	
	Archetype a = null
	
	if(obj instanceof Archetype) {
		a = obj as Archetype
		
	} else if(obj instanceof Closure) {
		
		a = new Archetype()
		final c = obj as Closure
		c.delegate = a
		c.resolveStrategy = Closure.DELEGATE_FIRST
		c()
	} else if(obj instanceof Map) {
		
		a = obj as Archetype
		
	} else {
		
		throw new IllegalArgumentException('Cannot register archetype using type [${obj.class.simpleName}] -- use a Map, a Closure, or an explicit Archetype instance.')
		
	}
	
	if(!a.id)
		throw new IllegalArgumentException('Cannot register archetype with no [id].')
	archetypes[a.id] = a
	
}

provides registerArchetype named 'register'

//
//
//