package org.snowjak.city.input.modifiers;

import org.snowjak.city.input.KeyTypedEvent;
import org.snowjak.city.input.hotkeys.Hotkey;

import com.badlogic.gdx.Input;

/**
 * A "modifier-key" is distinct from a {@link Hotkey} in two ways:
 * <ol>
 * <li>A Hotkey listens for {@link KeyTypedEvent}s, whereas a
 * {@link ModifierKey} listens for Key</li>
 * <li></li>
 * </ol>
 * 
 * @author snowjak88
 *
 */
public enum ModifierKey {
	
	SHIFT_LEFT(Input.Keys.SHIFT_LEFT), SHIFT_RIGHT(Input.Keys.SHIFT_RIGHT), SHIFT(Input.Keys.SHIFT_LEFT,
			Input.Keys.SHIFT_RIGHT), CONTROL_LEFT(Input.Keys.CONTROL_LEFT), CONTROL_RIGHT(
					Input.Keys.CONTROL_RIGHT), CONTROL(Input.Keys.CONTROL_LEFT,
							Input.Keys.CONTROL_RIGHT), ALT_LEFT(Input.Keys.ALT_LEFT), ALT_RIGHT(
									Input.Keys.ALT_RIGHT), ALT(Input.Keys.ALT_LEFT, Input.Keys.ALT_RIGHT);
	
	private final int[] keys;
	
	private ModifierKey(int... keys) {
		
		this.keys = keys;
	}
	
	/**
	 * Returns {@code true} if the given {@code keycode} matches any of this
	 * ModifierKey's configured keycodes.
	 * 
	 * @param keycode
	 * @return
	 */
	public boolean matches(int keycode) {
		
		for (int i = 0; i < keys.length; i++)
			if (keys[i] == keycode)
				return true;
		return false;
	}
}
