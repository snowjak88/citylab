package org.snowjak.city.tools

import com.badlogic.gdx.files.FileHandle

/**
 * Provides a visual mechanism for activating a Tool.
 * 
 * @author snowjak88
 *
 */
class ToolButton {
	private final FileHandle baseDirectory
	
	final Tool tool
	final String id
	
	String title, group
	FileHandle buttonUp, buttonDown
	
	public ToolButton(Tool tool, String id, FileHandle baseDirectory) {
		this.tool = tool
		this.id = id
		this.baseDirectory = baseDirectory
	}
	
	public void setButtonUp(String filename) {
		this.buttonUp = baseDirectory.child(filename)
	}
	
	public void setButtonUp(FileHandle file) {
		this.buttonUp = file;
	}
	
	public void setButtonDown(String filename) {
		this.buttonDown = baseDirectory.child(filename)
	}
	
	public void setButtonDown(FileHandle file) {
		this.buttonDown = file;
	}
}
