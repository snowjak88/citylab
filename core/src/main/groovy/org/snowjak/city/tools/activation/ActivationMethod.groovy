/**
 * 
 */
package org.snowjak.city.tools.activation

import org.snowjak.city.GameState

/**
 * @author snowjak88
 *
 */
interface ActivationMethod {
	
	public String getId()
	
	/**
	 * When this activation-method is "activated" (whatever that means),
	 * this handler should be executed
	 * @return
	 */
	public Runnable getActivateHandler()
	
	/**
	 * Register this ActivationMethod with the {@link GameState}
	 * @param state
	 */
	public abstract void register(GameState state)
}
