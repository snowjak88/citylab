/**
 * 
 */
package org.snowjak.city.util;

public class RelativePriority<V> {
	
	private V before, after;
	
	public RelativePriority<V> before(V before) {
		
		this.before = before;
		return this;
	}
	
	public RelativePriority<V> after(V after) {
		
		this.after = after;
		return this;
	}
	
	public V getBefore() {
		
		return before;
	}
	
	public V getAfter() {
		
		return after;
	}
	
}