/**
 * 
 */
package org.snowjak.city.input;

/**
 * @author snowjak88
 *
 */
public abstract class AbstractClickEvent extends AbstractInputEvent {
	
	private int x, y, button;
	
	public AbstractClickEvent() {
		
	}
	
	public int getX() {
		
		return x;
	}
	
	void setX(int X) {
		
		this.x = X;
	}
	
	public int getY() {
		
		return y;
	}
	
	void setY(int Y) {
		
		this.y = Y;
	}
	
	public int getButton() {
		
		return button;
	}
	
	void setButton(int button) {
		
		this.button = button;
	}
	
	@Override
	void reset() {
		
		x = 0;
		y = 0;
		button = 0;
	}
	
}
