/**
 * 
 */
package org.snowjak.city.tools.activation

import org.snowjak.city.GameState
import org.snowjak.city.tools.groups.ButtonToolGroup

import com.badlogic.gdx.files.FileHandle

/**
 * @author snowjak88
 *
 */
class ButtonActivationMethod extends GroupedActivationMethod<ButtonToolGroup> {
	
	final String id
	final Runnable activateHandler
	
	String title
	FileHandle buttonDown, buttonUp
	
	private final FileHandle baseDirectory
	
	public ButtonActivationMethod(String id, Map<String, ButtonToolGroup> groups, FileHandle baseDirectory, Runnable activateHandler) {
		
		super(groups)
		
		this.id = id
		this.baseDirectory = baseDirectory
		this.activateHandler = activateHandler
	}
	
	@Override
	protected ButtonToolGroup newDelegate(String id, Map<String,ButtonToolGroup> groups, FileHandle baseDirectory, ButtonToolGroup parent) {
		
		new ButtonToolGroup(id, this, groups, baseDirectory, parent)
	}
	
	@Override
	public void register(GameState state) {
		
		throw new UnsupportedOperationException()
	}
	
	public void setButtonDown(String buttonDownFilename) {
		buttonDown = baseDirectory.child(buttonDownFilename)
	}
	
	public void setButtonDown(FileHandle buttonDown) {
		
		this.buttonDown = buttonDown
	}
	
	public void setButtonUp(String buttonUpFilename) {
		buttonUp = baseDirectory.child(buttonUpFilename)
	}
	
	public void setButtonUp(FileHandle buttonUp) {
		
		this.buttonUp = buttonUp
	}
}
