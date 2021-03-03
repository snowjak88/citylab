/**
 * 
 */
package org.snowjak.city.mechanics.support;

import com.github.czyzby.lml.parser.LmlData;
import com.github.czyzby.lml.parser.impl.AbstractLmlParser;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;

/**
 * Enables scripted mechanics to create their own UI elements.
 * 
 * @author snowjak88
 *
 */
public interface UISupport {
	
	/**
	 * The main {@link MenuBar} has this ID.
	 */
	String ACTOR_MENUBAR = "menubar";
	/**
	 * The "File" {@link Menu} has this ID.
	 */
	String ACTOR_MENUBAR_FILE = "menubar-file";
	
	/**
	 * Create a {@link ScopedUISupport} instance. All calls to that instance will
	 * delegate to this UISupport instance, using the {@code extensionID} given
	 * here.
	 * 
	 * @param extensionID
	 * @return
	 */
	public ScopedUISupport getScopedUISupport(String extensionID);
	
	/**
	 * Create a new {@link Menu} within the main {@link MenuBar}, labeling it with
	 * the given label. {@code label} will be treated as an I18N bundle-key by
	 * default.
	 * 
	 * @param extensionID
	 * @param label
	 * @return
	 */
	public Menu addMenu(String extensionID, String label);
	
	/**
	 * Clean up any resources & elements allocated for the given
	 * {@code extensionID}.
	 * 
	 * @param extensionID
	 */
	public void cleanUpFor(String extensionID);
	
	/**
	 * This is largely copied verbatim from
	 * {@link AbstractLmlParser#parseBundleLine}, albeit with the following
	 * modifications:
	 * <ul>
	 * <li>In the original, the default bundle-name is
	 * {@link LmlData#getDefaultI18nBundle()}; here, it is {@code extensionID}</li>
	 * <li>In the original, you may supply <em>any</em> bundle-name explicitly;
	 * here, you may only supply {@code all} (signifying the default bundle)</li>
	 * </ul>
	 * 
	 * @param extensionID
	 * @param key
	 * @return
	 */
	public String translateBundleLine(String extensionID, String key);
}