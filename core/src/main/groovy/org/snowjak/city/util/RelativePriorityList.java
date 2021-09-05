/**
 * 
 */
package org.snowjak.city.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * @author snowjak88
 *
 */
public class RelativePriorityList<V, T extends RelativelyPrioritized<T, V>> implements List<T> {
	
	private final ArrayList<T> items = new ArrayList<>();
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * <strong>Note</strong> that this method throws a
	 * {@link PrioritizationFailedException} (wrapped in a {@link RuntimeException})
	 * if this item cannot be inserted due to conflicting priorities.
	 * </p>
	 */
	@Override
	public boolean add(T e) {
		
		//
		// If the list is empty, we can just add it.
		if (items.isEmpty())
			return items.add(e);
			
		//
		// Scan through the list until we find a spot for this item to go.
		for (int i = 0; i <= items.size(); i++) {
			
			if (itemFits(e, i)) {
				add(i, e);
				return true;
			}
			
		}
		
		throw new RuntimeException(new PrioritizationFailedException());
	}
	
	/**
	 * See if the given item will fit in this list at the given index (using the
	 * same semantics for {@code atIndex} as
	 * {@link #add(int, RelativelyPrioritized)}).
	 * 
	 * @param item
	 * @param atIndex
	 * @return
	 */
	private boolean itemFits(T item, int atIndex) {
		
		if (isEmpty())
			return true;
		
		final V itemKey = item.getRelativePriorityKey();
		
		//
		// Check "before" items.
		for (int i = 0; i < atIndex; i++) {
			
			final T beforeItem = get(i);
			final V beforeItemKey = beforeItem.getRelativePriorityKey();
			
			//
			// Does the current item need to come before the "before" item?
			// If so, it can't go in atIndex.
			for (V requiredBeforeKey : item.getRelativePriority().getBefore())
				if (requiredBeforeKey.equals(beforeItemKey))
					return false;
					
			//
			// Does the "before" item need to come after the current item?
			// If so, it can't go in atIndex.
			for (V requiredAfterKey : beforeItem.getRelativePriority().getAfter())
				if (requiredAfterKey.equals(itemKey))
					return false;
		}
		
		//
		// Haven't failed yet.
		//
		// Check "after" items.
		for (int i = atIndex; i < size(); i++) {
			
			final T afterItem = get(i);
			final V afterItemKey = afterItem.getRelativePriorityKey();
			
			//
			// Does the current item need to come after the "after" item?
			for (V requiredAfterKey : item.getRelativePriority().getAfter())
				if (requiredAfterKey.equals(afterItemKey))
					return false;
					
			//
			// Does the "after" item need to come before the current item?
			for (V requiredBeforeKey : afterItem.getRelativePriority().getBefore())
				if (requiredBeforeKey.equals(itemKey))
					return false;
		}
		
		//
		// This doesn't violate any priorities.
		// It might as well go here.
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * <strong>Note</strong> that using this method makes little sense with a
	 * RelativePriorityList.
	 * </p>
	 */
	@Override
	public void add(int index, T element) {
		
		items.add(index, element);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		
		return items.containsAll(c);
	}
	
	@Override
	public int size() {
		
		return items.size();
	}
	
	@Override
	public boolean isEmpty() {
		
		return items.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		
		return items.contains(o);
	}
	
	@Override
	public int indexOf(Object o) {
		
		return items.indexOf(o);
	}
	
	@Override
	public int lastIndexOf(Object o) {
		
		return items.lastIndexOf(o);
	}
	
	@Override
	public Object clone() {
		
		return items.clone();
	}
	
	@Override
	public Object[] toArray() {
		
		return items.toArray();
	}
	
	@Override
	public String toString() {
		
		return items.toString();
	}
	
	@Override
	public T get(int index) {
		
		return items.get(index);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * <strong>Note</strong> that using this method makes little sense with a
	 * RelativePriorityList.
	 * </p>
	 */
	@Override
	public T set(int index, T element) {
		
		return items.set(index, element);
	}
	
	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(IntFunction<T[]> generator) {
		
		return items.toArray(generator);
	}
	
	@Override
	public <U> U[] toArray(@SuppressWarnings("unchecked") U[] a) {
		
		return items.toArray(a);
	}
	
	@Override
	public T remove(int index) {
		
		return items.remove(index);
	}
	
	@Override
	public boolean equals(Object o) {
		
		return items.equals(o);
	}
	
	@Override
	public int hashCode() {
		
		return items.hashCode();
	}
	
	@Override
	public boolean remove(Object o) {
		
		return items.remove(o);
	}
	
	@Override
	public void clear() {
		
		items.clear();
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		
		boolean anyChanged = false;
		for (T value : c)
			anyChanged |= add(value);
		return anyChanged;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * <strong>Note</strong> that using this method makes little sense with a
	 * RelativePriorityList.
	 * </p>
	 */
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		
		return items.addAll(index, c);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		
		return items.removeAll(c);
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		
		return items.retainAll(c);
	}
	
	@Override
	public ListIterator<T> listIterator(int index) {
		
		return items.listIterator(index);
	}
	
	@Override
	public Stream<T> stream() {
		
		return items.stream();
	}
	
	@Override
	public ListIterator<T> listIterator() {
		
		return items.listIterator();
	}
	
	@Override
	public Iterator<T> iterator() {
		
		return items.iterator();
	}
	
	@Override
	public Stream<T> parallelStream() {
		
		return items.parallelStream();
	}
	
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		
		return items.subList(fromIndex, toIndex);
	}
	
	@Override
	public void forEach(Consumer<? super T> action) {
		
		items.forEach(action);
	}
	
	@Override
	public Spliterator<T> spliterator() {
		
		return items.spliterator();
	}
	
	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		
		return items.removeIf(filter);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * <strong>Note</strong> that using this method makes little sense with a
	 * RelativePriorityList.
	 * </p>
	 */
	@Override
	public void replaceAll(UnaryOperator<T> operator) {
		
		items.replaceAll(operator);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * <strong>Note</strong> that using this method makes little sense with a
	 * RelativePriorityList.
	 * </p>
	 */
	@Override
	public void sort(Comparator<? super T> c) {
		
		items.sort(c);
	}
}
