package org.snowjak.city.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

/**
 * Indicates that a cell cannot be built on -- whether or not it is occupied, does it have some attribute that makes it unsuitable for building?
 * 
 * @author snowjak88
 *
 */
class IsNonBuildableCell implements Component, Poolable {
	
	/**
	 * If one of your systems marks a cell as non-buildable, you must:
	 * <ul>
	 * <li>ensure that you check for the pre-existence of this IsNonBuildableCell component before creating a new one</li>
	 * <li>ensure that you add your own unique blocker-ID to identify your specific blocker-type. (it only has to be unique from all other blocker-IDs)</li>
	 * <li>when your system removes the blocker, you must remove your blocker-ID from this component, too</li>
	 * </ul>
	 */
	final Set<String> blockerIDs = []
	
	@Override
	public void reset() {
		blockerIDs.clear()
	}
}
