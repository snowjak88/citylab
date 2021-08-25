/**
 * 
 */
package org.snowjak.city.util;

/**
 * @author snowjak88
 *
 */
public interface RelativelyPrioritized<T, V> {
	
	public V getRelativePriorityKey();
	
	public RelativePriority<V> getRelativePriority();
}
