/**
 * 
 */
package org.snowjak.city.util;

/**
 * Certifies the bearer to unregister something from something else.
 * 
 * @author snowjak88
 *
 */
@FunctionalInterface
public interface UnregistrationHandle {
	
	public void unregisterMe();
}