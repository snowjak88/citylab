/**
 * 
 */
package org.snowjak.city.util;

import java.util.LinkedList;

/**
 * Like {@link LinkedList}, but all the values are {@code int}s.
 * 
 * @author snowjak88
 *
 */
public class LinkedIntList {
	
	private Item root;
	private int size = 0;
	
	public LinkedIntList() {
		
	}
	
	public void addAll(int... values) {
		
		for (int value : values)
			add(value);
	}
	
	public void add(int value) {
		
		add(value, false);
	}
	
	public void addFirst(int value) {
		
		add(value, true);
	}
	
	public void addLast(int value) {
		
		add(value, false);
	}
	
	private void add(int value, boolean asFirst) {
		
		size++;
		
		if (root == null) {
			root = new Item(null, value);
			root.prev = root;
			return;
		}
		
		final Item last = root.prev;
		
		if (asFirst) {
			final Item newRoot = new Item(last, value);
			newRoot.next = root;
			root.prev = newRoot;
			root = newRoot;
			
			return;
		}
		
		final Item next = new Item(last, value);
		
		last.next = next;
		root.prev = next;
	}
	
	public boolean remove(int value) {
		
		return remove(value, false);
	}
	
	public boolean removeAll(int value) {
		
		return remove(value, true);
	}
	
	public boolean remove(int... values) {
		
		boolean removedAny = false;
		for (int value : values)
			removedAny |= remove(value);
		return removedAny;
	}
	
	public boolean removeAll(int... values) {
		
		boolean removedAny = false;
		for (int value : values)
			removedAny |= removeAll(value);
		return removedAny;
	}
	
	private boolean remove(int value, boolean removeAll) {
		
		boolean removedAny = false;
		
		if (root.value == value) {
			
			if (root.next == null)
				root = null;
			else {
				final Item newRoot = root.next;
				newRoot.prev = root.prev;
				root = newRoot;
			}
			
			removedAny = true;
			if (!removeAll)
				return removedAny;
		}
		
		Item current = root;
		while (current.next != null) {
			current = current.next;
			if (current.value == value) {
				final Item prev = current.prev, next = current.next;
				prev.next = next;
				if (next != null)
					next.prev = prev;
				
				removedAny = true;
				if (!removeAll)
					return true;
			}
		}
		
		return removedAny;
	}
	
	public int[] toArray() {
		
		if (size == 0)
			return new int[0];
		
		final int[] result = new int[size];
		int index = 0;
		Item current = root;
		while (current != null) {
			result[index] = current.value;
			current = current.next;
		}
		
		return result;
	}
	
	private static class Item {
		
		Item prev, next;
		int value;
		
		Item(Item prev, int value) {
			
			this.prev = prev;
			this.value = value;
		}
		
	}
}
