/**
 * 
 */
package org.snowjak.city.tools.groups

import javax.naming.OperationNotSupportedException

import org.snowjak.city.tools.activation.GroupedActivationMethod

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.Actor

/**
 * @author snowjak88
 *
 */
class MenuToolGroup extends ToolGroup<MenuToolGroup> {
	
	public MenuToolGroup(String id, GroupedActivationMethod context = null, Map<String,MenuToolGroup> groups, FileHandle baseDirectory, MenuToolGroup parent = null) {
		super(id, context, groups, baseDirectory, parent)
	}
	
	@Override
	protected MenuToolGroup newDelegate(String id, Map<String,MenuToolGroup> groups, FileHandle baseDirectory, MenuToolGroup parent) {
		
		new MenuToolGroup(id, context, groups, baseDirectory, parent)
	}
	
	
	@Override
	public Actor getRepresentation() {
		
		throw new OperationNotSupportedException()
	}
}
