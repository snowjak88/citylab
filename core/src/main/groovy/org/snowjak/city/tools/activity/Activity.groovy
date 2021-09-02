package org.snowjak.city.tools.activity

import org.snowjak.city.tools.Tool

/**
 * A Tool activity is called every frame while a Tool is active.
 * @author snowjak88
 *
 */
public abstract class Activity {
	
	final Tool tool
	
	public Activity(Tool tool) {
		this.tool = tool
	}
	
	/**
	 * Called when this activity's Tool is activated. The default implementation does nothing.
	 */
	public void activate() {
	}
	
	/**
	 * Called every frame.
	 */
	public abstract void update()
	
	/**
	 * Called when this activity's Tool is deactivated. The default implementation does nothing.
	 */
	public void deactivate() {
	}
}
