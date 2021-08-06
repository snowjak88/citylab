/**
 * 
 */
package org.snowjak.city.input;

import org.snowjak.city.input.GameInputProcessor.Unregisterable;

public interface ClickEventReceiver extends Unregisterable {
	
	/**
	 * Called when a mouse-click event occurs.
	 * 
	 * @param screenX
	 * @param screenY
	 * @param button
	 */
	public void click(int screenX, int screenY, int button);
}