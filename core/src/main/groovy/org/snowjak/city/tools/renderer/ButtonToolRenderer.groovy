/**
 * 
 */
package org.snowjak.city.tools.renderer

import org.snowjak.city.tools.Tool
import org.snowjak.city.tools.groups.ButtonToolGroup

import com.badlogic.gdx.scenes.scene2d.Stage

/**
 * Handles rendering buttons for {@link ButtonActivationMethod button-activated} {@link Tool}s.
 * @author snowjak88
 *
 */
class ButtonToolRenderer {
	
	Stage stage
	
	private final Map<String,Tool> tools = new LinkedHashMap<>()
	
	/**
	 * Register all buttons defined by the given {@link Tool}.
	 * @param tool
	 */
	public void addTool(Tool tool) {
		
		tools << ["$tool.id" : tool]
	}
}
