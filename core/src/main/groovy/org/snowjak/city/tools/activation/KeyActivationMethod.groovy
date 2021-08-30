/**
 * 
 */
package org.snowjak.city.tools.activation

import org.snowjak.city.GameState
import org.snowjak.city.input.hotkeys.Hotkey

/**
 * @author snowjak88
 *
 */
class KeyActivationMethod extends Hotkey implements ActivationMethod {
	
	final Runnable activateHandler
	
	public KeyActivationMethod(String id, Runnable activateHandler) {
		
		super(id)
		this.activateHandler = activateHandler
	}
	
	@Override
	public void register(GameState state) {
		state.hotkeys.register this, activateHandler
	}
}
