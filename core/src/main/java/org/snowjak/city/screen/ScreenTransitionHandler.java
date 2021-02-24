/**
 * 
 */
package org.snowjak.city.screen;

/**
 * Describes a callback whereby a {@link Screen} can indicate that it should be
 * replaced with another {@link Screen}.
 * 
 * @author snowjak88
 *
 */
@FunctionalInterface
public interface ScreenTransitionHandler {
	
	/**
	 * Replace the currently-active {@link Screen} with the given Screen instance.
	 * 
	 * @param nextScreen
	 */
	public void handle(Screen nextScreen);
}
