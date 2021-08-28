/**
 * 
 */
package org.snowjak.city.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class RelativePriority<V> {
	
	private Set<V> before = new LinkedHashSet<>(), after = new LinkedHashSet<>();
	
	@SuppressWarnings("unchecked")
	public RelativePriority<V> before(V... before) {
		
		this.before.addAll(Arrays.asList(before));
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public RelativePriority<V> after(V... after) {
		
		this.after.addAll(Arrays.asList(after));
		return this;
	}
	
	public Set<V> getBefore() {
		
		return before;
	}
	
	public Set<V> getAfter() {
		
		return after;
	}
	
}