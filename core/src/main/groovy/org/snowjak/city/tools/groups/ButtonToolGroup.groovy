/**
 * 
 */
package org.snowjak.city.tools.groups

import org.snowjak.city.tools.activation.GroupedActivationMethod

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.Actor

/**
 * @author snowjak88
 *
 */
class ButtonToolGroup extends ToolGroup<ButtonToolGroup> {
	
	FileHandle buttonUp, buttonDown
	
	public ButtonToolGroup(String id, GroupedActivationMethod context = null, Map<String,MenuToolGroup> groups, FileHandle baseDirectory) {
		
		super(id, context, groups, baseDirectory)
	}
	
	@Override
	protected ButtonToolGroup newDelegate() {
		
		new ButtonToolGroup(context)
	}
	
	@Override
	public Actor getRepresentation() {
		
		throw new UnsupportedOperationException()
	}
	
	
	public void setButtonUp(String buttonUpFilename) {
		
		buttonUp = baseDirectory.child(buttonUpFilename)
	}
	
	public void setButtonUp(FileHandle buttonUp) {
		
		this.buttonUp = buttonUp
	}
	
	public void setButtonDown(String buttonDownFilename) {
		
		buttonDown = baseDirectory.child(buttonDownFilename)
	}
	
	public void setButtonDown(FileHandle buttonDown) {
		
		this.buttonDown = buttonDown
	}
}
