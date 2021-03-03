/**
 * 
 */
package org.snowjak.city.mechanics.support;

import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;

/**
 * Enables UI elements to be created for a specific extension.
 * 
 * @author snowjak88
 *
 */
public class ScopedUISupport {
	
	private final UISupport support;
	private final String extensionID;
	
	ScopedUISupport(UISupport support, String extensionID) {
		
		this.support = support;
		this.extensionID = extensionID;
	}
	
	/**
	 * Translate the given {@code key} using the configured bundle, if any.
	 * <p>
	 * You may prefix your key with {@code all} to refer to the default
	 * (application-wide) bundle.
	 * </p>
	 * 
	 * @param key
	 * @return
	 */
	public String bundle(String key) {
		
		return support.translateBundleLine(extensionID, key);
	}
	
	/**
	 * Adds a {@link Menu} to the end of the main {@link MenuBar}, and returns the
	 * new Menu instance so it can be manipulated.
	 * 
	 * @param label
	 * @return
	 */
	public Menu addMenu(String label) {
		
		return support.addMenu(extensionID, label);
	}
	
	/**
	 * Disposes of all elements created with this instance.
	 */
	public void cleanUp() {
		
		support.cleanUpFor(extensionID);
	}
}
