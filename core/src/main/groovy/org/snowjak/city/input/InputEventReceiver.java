/**
 * 
 */
package org.snowjak.city.input;

/**
 * @author snowjak88
 *
 */
@FunctionalInterface
public interface InputEventReceiver<E extends AbstractInputEvent> {
	
	public void receive(E event);
}
