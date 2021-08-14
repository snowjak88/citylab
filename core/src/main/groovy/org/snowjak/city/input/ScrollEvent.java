/**
 * 
 */
package org.snowjak.city.input;

/**
 * @author snowjak88
 *
 */
public class ScrollEvent extends AbstractInputEvent {
	
	private float amountX, amountY;
	
	public ScrollEvent() {
		
	}
	
	public float getAmountX() {
		
		return amountX;
	}
	
	void setAmountX(float amountX) {
		
		this.amountX = amountX;
	}
	
	public float getAmountY() {
		
		return amountY;
	}
	
	void setAmountY(float amountY) {
		
		this.amountY = amountY;
	}
	
	@Override
	void reset() {
		
		amountX = 0;
		amountY = 0;
	}
}
