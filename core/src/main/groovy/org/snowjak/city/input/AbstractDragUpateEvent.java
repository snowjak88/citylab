/**
 * 
 */
package org.snowjak.city.input;

/**
 * @author snowjak88
 *
 */
public abstract class AbstractDragUpateEvent extends AbstractInputEvent {
	
	private int x, y;
	
	public AbstractDragUpateEvent() {
		
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
	
	@Override
	void reset() {
		
		x = 0;
		y = 0;
	}
	
}
