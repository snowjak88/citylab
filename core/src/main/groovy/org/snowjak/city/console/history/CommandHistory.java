/**
 * 
 */
package org.snowjak.city.console.history;

/**
 * @author snowjak88
 *
 */
public interface CommandHistory {
	
	/**
	 * Add a new command to the end of the history.
	 * 
	 * @param command
	 */
	public void add(String command);
	
	/**
	 * Get the previous command in the history -- i.e., that command which was added
	 * immediately before the command last returned by {@link #getPrevious()}.
	 * <p>
	 * If we've already returned the very first command in this history, this method
	 * returns the first command in this history again.
	 * </p>
	 * 
	 * @return
	 */
	public String getPrevious();
	
	/**
	 * Get the next command in the history -- i.e., that command which was added
	 * immediately after the command last returned by {@link #getNext()}.
	 * <p>
	 * If we've already returned the very last command in this history, this method
	 * returns the empty string.
	 * </p>
	 * 
	 * @return
	 */
	public String getNext();
}
