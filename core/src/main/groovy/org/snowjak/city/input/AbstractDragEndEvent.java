/**
 * 
 */
package org.snowjak.city.input;

/**
 * @author snowjak88
 *
 */
public abstract class AbstractDragEndEvent extends AbstractInputEvent {
	
	private int x, y, button;
	
	public AbstractDragEndEvent() {
		
	}
	
	public int getX() {
		
		return x;
	}
	
	void setX(int x) {
		
		this.x = x;
	}
	
	public int getY() {
		
		return y;
	}
	
	void setY(int y) {
		
		this.y = y;
	}
	
	public int getButton() {
		
		return button;
	}
	
	public void setButton(int button) {
		
		this.button = button;
	}
	
	@Override
	void reset() {
		
		x = 0;
		y = 0;
		button = 0;
	}
	
}
