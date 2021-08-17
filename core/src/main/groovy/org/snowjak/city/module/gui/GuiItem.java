/**
 * 
 */
package org.snowjak.city.module.gui;

import org.snowjak.city.util.UnregistrationHandle;

/**
 * @author snowjak88
 *
 */
public interface GuiItem {
	
	public UnregistrationHandle registerClickListener();
	
	/**
	 * Remove this item from the GUI. Unregister all callbacks etc.
	 */
	public void remove();
}
