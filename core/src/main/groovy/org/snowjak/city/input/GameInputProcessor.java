/**
 * 
 */
package org.snowjak.city.input;

import org.snowjak.city.controller.CityMapScreen;
import org.snowjak.city.controller.CityMapScreen.MapScrollControl;
import org.snowjak.city.controller.CityMapScreen.ZoomControl;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

/**
 * Handles input for the {@link CityMapScreen} (excepting those input-events
 * that should be handled by the menu-system).
 * 
 * @author snowjak88
 *
 */
public class GameInputProcessor extends InputAdapter {
	
	private ZoomControl zoomControl;
	private MapScrollControl scrollControl;
	
	private int startDragX = 0, startDragY = 0;
	
	public GameInputProcessor(ZoomControl zoomControl, MapScrollControl scrollControl) {
		
		this.zoomControl = zoomControl;
		this.scrollControl = scrollControl;
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		
		if (button != Input.Buttons.LEFT)
			return false;
		
		startDragX = screenX;
		startDragY = screenY;
		return true;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		
		scrollControl.scrollBy(startDragX, startDragY, screenX, screenY);
		startDragX = screenX;
		startDragY = screenY;
		return true;
	}
	
	@Override
	public boolean scrolled(float amountX, float amountY) {
		
		if (amountY > 0)
			zoomControl.zoomIn();
		
		if (amountY < 0)
			zoomControl.zoomOut();
		
		return true;
	}
	
}
