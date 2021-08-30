/**
 * 
 */
package org.snowjak.city.input;

/**
 * @author snowjak88
 *
 */
public class KeyTypedEvent extends AbstractInputEvent {
	
	private int keycode;
	private boolean isAlt, isCtrl, isShift;
	
	public int getKeycode() {
		
		return keycode;
	}
	
	public void setKeycode(int keycode) {
		
		this.keycode = keycode;
	}
	
	public boolean isAlt() {
		
		return isAlt;
	}
	
	public void setAlt(boolean isAlt) {
		
		this.isAlt = isAlt;
	}
	
	public boolean isCtrl() {
		
		return isCtrl;
	}
	
	public void setCtrl(boolean isCtrl) {
		
		this.isCtrl = isCtrl;
	}
	
	public boolean isShift() {
		
		return isShift;
	}
	
	public void setShift(boolean isShift) {
		
		this.isShift = isShift;
	}
	
	@Override
	void reset() {
		
		keycode = 0;
		isAlt = false;
		isCtrl = false;
		isShift = false;
	}
}
