/**
 * 
 */
package org.snowjak.city.console.executors;

import org.snowjak.city.console.Console;

/**
 * @author snowjak88
 *
 */
public abstract class AbstractConsoleExecutor {
	
	private final Console console;
	
	public AbstractConsoleExecutor(Console console) {
		
		this.console = console;
	}
	
	/**
	 * Execute the given command-string. Ensures that any output (including errors)
	 * is written back to the {@link Console}.
	 * 
	 * @param command
	 */
	public abstract void execute(String command);
	
	public Console getConsole() {
		
		return console;
	}
}
