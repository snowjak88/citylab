/**
 * 
 */
package org.snowjak.city.input;

/**
 * @author snowjak88
 *
 */
public abstract class AbstractDragEndEvent extends AbstractInputEvent {
	
	private int x, y;
	
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
	
	@Override
	void reset() {
		
		x = 0;
		y = 0;
	}
	
}
