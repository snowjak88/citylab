/**
 * 
 */
package org.snowjak.city.tools.activation

import org.snowjak.city.GameState
import org.snowjak.city.tools.groups.MenuToolGroup

import com.badlogic.gdx.files.FileHandle

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
	protected MenuToolGroup newDelegate(String id, Map<String,MenuToolGroup> groups, FileHandle baseDirectory, MenuToolGroup parent) {
		
		new MenuToolGroup(id, this, groups, baseDirectory, parent)
	}
	
	@Override
	public void register(GameState state) {
		
		throw new UnsupportedOperationException()
	}
}
