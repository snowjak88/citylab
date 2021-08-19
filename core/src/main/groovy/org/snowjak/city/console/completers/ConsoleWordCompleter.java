/**
 * 
 */
package org.snowjak.city.console.completers;

import java.util.List;

/**
 * @author snowjak88
 *
 */
public interface ConsoleWordCompleter {
	
	/**
	 * Given a {@code command} that's being typed, find the list of words that could
	 * be used next.
	 * <p>
	 * This may be as simple as detecting the last word in the command and looking
	 * for ways to finish it. It might also mean analyzing the whole command and
	 * suggesting additions. Details are left to the implementation.
	 * </p>
	 * 
	 * @param command
	 * @return A list of possible commands, all of which are "completed". Returns an
	 *         empty list if no completions were identified.
	 */
	public List<String> complete(String command);
	
}
