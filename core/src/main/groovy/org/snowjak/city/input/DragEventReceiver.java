/**
 * 
 */
package org.snowjak.city.input;

import org.snowjak.city.input.GameInputProcessor.Unregisterable;

public interface DragEventReceiver extends Unregisterable {
	
	/**
	 * Called when a mouse-drag event starts.
	 * 
	 * @param screenX
	 * @param screenY
	 * @param button
	 */
	public void dragStart(int screenX, int screenY, int button);
	
	/**
	 * Called when a mouse-drag event is updated to a new location.
	 * 
	 * @param screenX
	 * @param screenY
	 */
	public void dragUpdate(int screenX, int screenY);
	
	/**
	 * Called when a mouse-drag event ends.
	 * 
	 * @param screenX
	 * @param screenY
	 */
	public void dragEnd(int screenX, int screenY);
}