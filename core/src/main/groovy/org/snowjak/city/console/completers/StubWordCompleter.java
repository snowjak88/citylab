/**
 * 
 */
package org.snowjak.city.console.completers;

import java.util.ArrayList;
import java.util.List;

/**
 * Dumb implementation of {@link ConsoleWordCompleter}. Just returns whatever
 * you pass in, with 3 variations:
 * <ul>
 * <li>string + "A"</li>
 * <li>string + "AA"</li>
 * <li>string + "AAA"</li>
 * </ul>
 * 
 * @author snowjak88
 *
 */
public class StubWordCompleter implements ConsoleWordCompleter {
	
	@Override
	public List<String> complete(String command) {
		
		final List<String> result = new ArrayList<>();
		
		result.add(command);
		result.add(command + "A");
		result.add(command + "AA");
		result.add(command + "AAA");
		
		return result;
	}
}
