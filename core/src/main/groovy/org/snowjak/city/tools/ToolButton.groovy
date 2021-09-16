package org.snowjak.city.tools

import com.badlogic.gdx.files.FileHandle

/**
 * Provides a visual mechanism for activating a Tool.
 * 
 * @author snowjak88
 *
 */
class ToolButton {
	
	final FileHandle baseDirectory
	final Tool tool
	final String id
	
	String group
	String buttonUp, buttonDown
	
	public ToolButton(Tool tool, String id, FileHandle baseDirectory) {
		this.tool = tool
		this.id = id
		this.baseDirectory = baseDirectory
	}
}
