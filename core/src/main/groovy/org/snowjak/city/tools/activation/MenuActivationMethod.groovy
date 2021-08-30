/**
 * 
 */
package org.snowjak.city.tools.activation

import org.snowjak.city.GameState
import org.snowjak.city.tools.groups.MenuToolGroup

/**
 * @author snowjak88
 *
 */
class MenuActivationMethod extends GroupedActivationMethod<MenuToolGroup> {
	
	final String id
	final Runnable activateHandler
	
	String text
	
	public MenuActivationMethod(String id, Map<String, MenuToolGroup> groups, Runnable activateHandler) {
		
		super(groups)
		
		this.id = id
		this.activateHandler = activateHandler
	}
	
	@Override
	protected MenuToolGroup newDelegate() {
		
		new MenuToolGroup(this)
	}
	
	@Override
	public void register(GameState state) {
		
		throw new UnsupportedOperationException()
	}
}
