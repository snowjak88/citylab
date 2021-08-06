/**
 * 
 */
package org.snowjak.city.input;

public interface ScrollEventReceiver {
	
	/**
	 * Called when a scroll-wheel event occurs.
	 * 
	 * @param amountX
	 * @param amountY
	 */
	public void scroll(float amountX, float amountY);
}
