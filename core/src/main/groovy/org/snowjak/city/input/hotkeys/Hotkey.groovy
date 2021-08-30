/**
 * 
 */
package org.snowjak.city.input.hotkeys

import org.snowjak.city.input.KeyTypedEvent

import com.badlogic.gdx.Input

/**
 * Represents a particular combination of keys.
 * 
 * @author snowjak88
 *
 */
class Hotkey {
	
	final String id = ""
	
	String title = ""
	int keycode = 0
	boolean isCtrl = false, isAlt = false, isShift = false
	
	boolean isColliding = false
	
	public Hotkey(String id) {
		this.id = id
	}
	
	public static Hotkey valueOf(String value) {
		final Hotkey result = new Hotkey()
		
		result.keys = value
		
		result
	}
	
	public String toString() {
		((isAlt) ? 'Alt+' : '' ) +
				((isCtrl) ? 'Ctrl+' : '' ) +
				((isShift) ? 'Shift+' : '' ) +
				Input.Keys.toString(keycode)
	}
	
	public void setKeys(String hotkeyString) {
		if(hotkeyString == null)
			throw new NullPointerException("Cannot extract Hotkey from a null String!")
		
		if(hotkeyString.isBlank())
			throw new IllegalArgumentException("Cannot extract Hotkey from a blank String!")
		
		hotkeyString = hotkeyString.trim()
		
		def isAlt = false
		def isCtrl = false
		def isShift = false
		
		if(hotkeyString.startsWithIgnoreCase("Alt+")) {
			isAlt = true
			hotkeyString = hotkeyString.drop(4)
		}
		if(hotkeyString.startsWithIgnoreCase("Ctrl+")) {
			isCtrl = true
			hotkeyString = hotkeyString.drop(5)
		}
		if(hotkeyString.startsWithIgnoreCase("Shift+")) {
			isShift = true
			hotkeyString = hotkeyString.drop(6)
		}
		
		def keycode = Input.Keys.valueOf(hotkeyString)
		if(keycode > -1) {
			this.isAlt = isAlt
			this.isCtrl = isCtrl
			this.isShift = isShift
			this.keycode = keycode
		}
		else
			throw new IllegalArgumentException("Cannot unpack hotkey-string \"$hotkeyString\".")
	}
	
	
	/**
	 * Returns {@code true} if this Hotkey matches the given {@link KeyTypedEvent event},
	 * <strong>and</strong> if this Hotkey is not marked as currently colliding with any other.
	 */
	public boolean matches(KeyTypedEvent event) {
		(!isColliding) &&
				(isAlt == event.alt) &&
				(isCtrl == event.ctrl) &&
				(isShift == event.shift) &&
				(keycode == event.keycode)
	}
	
	/**
	 * Returns {@code true} if this Hotkey matches the other Hotkey -- i.e., if they would both
	 * {@link #matches(KeyTypedEvent) match} on the same KeyTypedEvent.
	 * @param other
	 * @return
	 */
	public boolean matches(Hotkey other) {
		(isAlt == other.isAlt) &&
				(isCtrl == other.isCtrl) &&
				(isShift == other.isShift) &&
				(keycode == other.keycode)
	}
}
