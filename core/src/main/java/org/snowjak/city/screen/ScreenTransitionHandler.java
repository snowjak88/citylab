/**
 * 
 */
package org.snowjak.city.screen;

/**
 * Describes a callback whereby a {@link AbstractScreen} can indicate that it should be
 * replaced with another {@link AbstractScreen}.
 * 
 * @author snowjak88
 *
 */
@FunctionalInterface
public interface ScreenTransitionHandler {
	
	/**
	 * Replace the currently-active {@link AbstractScreen} with the given Screen instance.
	 * 
	 * @param nextScreen
	 */
	public void handle(AbstractScreen nextScreen);
}
