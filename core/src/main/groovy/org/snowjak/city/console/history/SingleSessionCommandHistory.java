/**
 * 
 */
package org.snowjak.city.console.history;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * A {@link CommandHistory} that only preserves a single session's history. Once
 * the session is terminated, the command-history is lost.
 * 
 * @author snowjak88
 *
 */
public class SingleSessionCommandHistory implements CommandHistory {
	
	private final int maxHistoryLength;
	
	private final LinkedList<String> history = new LinkedList<>();
	
	private ListIterator<String> historyIterator = history.listIterator();
	private String previous, next;
	
	/**
	 * Construct a new {@link SingleSessionCommandHistory} with a maximum
	 * history-length of 1024 commands.
	 */
	public SingleSessionCommandHistory() {
		
		this(1024);
	}
	
	/**
	 * Construct a new {@link SingleSessionCommandHistory} with the given maximum
	 * history-length.
	 * 
	 * @param maxHistoryLength
	 * @throws IllegalArgumentException
	 *             if {@code maxHistoryLength} is less than 1
	 */
	public SingleSessionCommandHistory(int maxHistoryLength) {
		
		if (maxHistoryLength < 1)
			throw new IllegalArgumentException(
					"You cannot instantiate a command-history with a maximum history-length of less than 1 item!");
		
		this.maxHistoryLength = maxHistoryLength;
	}
	
	@Override
	public void add(String command) {
		
		synchronized (this) {
			history.addLast(command);
			historyIterator = history.listIterator(history.size());
			
			trimHistory();
		}
	}
	
	@Override
	public String getPrevious() {
		
		synchronized (this) {
			if (historyIterator.hasPrevious())
				previous = historyIterator.previous();
			
			return previous;
		}
	}
	
	@Override
	public String getNext() {
		
		synchronized (this) {
			if (historyIterator.hasNext())
				next = historyIterator.next();
			else
				next = "";
			
			return next;
		}
	}
	
	private void trimHistory() {
		
		synchronized (this) {
			boolean removedAny = false;
			
			while (history.size() > maxHistoryLength) {
				history.removeFirst();
				removedAny = true;
			}
			
			if (removedAny)
				historyIterator = history.listIterator(historyIterator.nextIndex());
		}
	}
}
