/**
 * 
 */
package org.snowjak.city.service;

import java.util.LinkedHashMap;
import java.util.Map;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.czyzby.autumn.annotation.Component;

/**
 * Serves as a repository for {@link Skin} instances.
 * 
 * @author snowjak88
 *
 */
@Component
public class SkinService {
	
	private final Map<String, Skin> skins = new LinkedHashMap<>();
	
	public void addSkin(String skinName, Skin skin) {
		
		synchronized (this) {
			skins.put(skinName, skin);
		}
	}
	
	public Skin getSkin(String skinName) {
		
		synchronized (this) {
			return skins.get(skinName);
		}
	}
}
