/**
 * 
 */
package org.snowjak.city.screens.menupages;

import org.snowjak.city.screens.MainMenuScreen;

import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * @author snowjak88
 *
 */
public interface MainMenuPage {
	
	public Actor getRoot(MainMenuScreen mainMenuScreen);
	
	public void show();
	
	public void hide();
}
