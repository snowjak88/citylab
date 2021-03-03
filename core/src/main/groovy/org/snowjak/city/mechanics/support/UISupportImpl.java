/**
 * 
 */
package org.snowjak.city.mechanics.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.I18NBundle;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.lml.parser.LmlData;
import com.github.czyzby.lml.parser.LmlSyntax;
import com.github.czyzby.lml.util.LmlUtilities;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;

public class UISupportImpl implements UISupport {
	
	private final InterfaceService interfaceService;
	
	private final MenuBar menuBar;
	
	private final Map<String, ScopedUISupport> scopedInstances = new HashMap<>();
	private final Map<String, Collection<Actor>> actors = new HashMap<>();
	
	public UISupportImpl(InterfaceService interfaceService, MenuBar mainMenuBar) {
		
		this.interfaceService = interfaceService;
		this.menuBar = mainMenuBar;
	}
	
	@Override
	public ScopedUISupport getScopedUISupport(String extensionID) {
		
		actors.computeIfAbsent(extensionID, (id) -> new LinkedHashSet<>());
		return scopedInstances.computeIfAbsent(extensionID, (id) -> new ScopedUISupport(this, id));
	}
	
	@Override
	public Menu addMenu(String extensionID, String label) {
		
		final Menu menu = new Menu(translateBundleLine(extensionID, label));
		
		menuBar.addMenu(menu);
		return menu;
	}
	
	@Override
	public void cleanUpFor(String extensionID) {
		
		scopedInstances.remove(extensionID);
		if (actors.containsKey(extensionID)) {
			for (Actor actor : actors.get(extensionID))
				actor.remove();
			actors.remove(extensionID);
		}
	}
	
	@Override
	public String translateBundleLine(String extensionID, String key) {
		
		final LmlSyntax syntax = interfaceService.getParser().getSyntax();
		final LmlData data = interfaceService.getParser().getData();
		String bundleKey = LmlUtilities.stripMarker(key, syntax.getBundleLineMarker());
		
		I18NBundle bundle = null;
		
		if (Strings.contains(key, syntax.getIdSeparatorMarker())) {
			
			// Bundle name is given, as bundle key contains separator. Extracting specific
			// bundle.
			final int separatorIndex = bundleKey.indexOf(syntax.getIdSeparatorMarker());
			final String bundleName = bundleKey.substring(0, separatorIndex);
			
			if (bundleName.equalsIgnoreCase("all")) {
				bundle = data.getDefaultI18nBundle();
				bundleKey = bundleKey.substring(separatorIndex + 1, bundleKey.length());
			}
		} else {
			
			// No specific bundle name. Using the extension's bundle, if any.
			bundle = data.getI18nBundle(extensionID);
			bundleKey = key;
		}
		
		if (bundle == null)
			//
			// Apparently we couldn't find a valid bundle. That's a failure-condition.
			return key;
		
		if (Strings.contains(key, syntax.getBundleLineArgumentMarker())) {
			//
			// All right. This key contains arguments that need to be
			// parsed out and fed as an array into the appropriate formatter.
			final String[] keyAndArguments = Strings.split(bundleKey, syntax.getBundleLineArgumentMarker());
			final String[] onlyArguments = new String[keyAndArguments.length - 1];
			for (int i = 0; i < onlyArguments.length; i++)
				onlyArguments[i] = keyAndArguments[i + 1];
			
			return bundle.format(keyAndArguments[0], (Object[]) onlyArguments);
		}
		
		return bundle.get(bundleKey);
		
	}
}
