/**
 * 
 */
package org.snowjak.city.util;

/**
 * Function that accepts some value and produces a {@code float} value.
 * 
 * @author snowjak88
 *
 */
@FunctionalInterface
public interface ToFloatFunction<V> {
	
	public float apply(V value);
}
