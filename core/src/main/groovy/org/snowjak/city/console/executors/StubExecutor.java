/**
 * 
 */
package org.snowjak.city.console.executors;

import org.snowjak.city.console.Console;

/**
 * Stub executor. No matter what you provide, it spits out the same message.
 * 
 * @author snowjak88
 *
 */
public class StubExecutor extends AbstractConsoleExecutor {
	
	private int i = 0;
	
	/**
	 * @param console
	 */
	public StubExecutor(Console console) {
		
		super(console);
	}
	
	@Override
	public void execute(String command) {
		
		getConsole().write("(" + i + ") You wrote \"" + command + "\".");
		i++;
	}
}
