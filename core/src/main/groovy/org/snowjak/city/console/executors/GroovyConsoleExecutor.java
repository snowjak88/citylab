/**
* 
*/
package org.snowjak.city.console.executors;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.snowjak.city.console.Console;
import org.snowjak.city.console.ConsolePrintStream;
import org.snowjak.city.console.model.ConsoleModel;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.DelegatingScript;

/**
 * @author snowjak88
 *
 */
public class GroovyConsoleExecutor extends AbstractConsoleExecutor {
	
	private final StringBuffer multiLineCommand;
	
	private final ConsoleModel model;
	private final Binding binding = new Binding();
	private final GroovyShell shell;
	
	public GroovyConsoleExecutor(Console console, ConsoleModel model, ConsolePrintStream printStream) {
		
		super(console);
		this.model = model;
		
		this.multiLineCommand = new StringBuffer();
		
		//
		// Set up our PrintStream to capture normal output
		binding.setProperty("out", printStream);
		
		shell = new GroovyShell(binding, getCompilerConfig());
	}
	
	@Override
	public void execute(String command) {
		
		if (command == null || command.trim().isEmpty())
			return;
		
		try {
			
			if (multiLineCommand.length() > 0)
				getConsole().println(command);
			else
				getConsole().println("> ", command);
			
			if (command.endsWith("\\")) {
				multiLineCommand.append("\n");
				final String trimmedCommand = command.substring(0, command.lastIndexOf("\\"));
				multiLineCommand.append(trimmedCommand);
				
				return;
			}
			
			final String toExecute;
			if (multiLineCommand.length() > 0) {
				multiLineCommand.append("\n");
				multiLineCommand.append(command);
				toExecute = multiLineCommand.toString();
				multiLineCommand.setLength(0);
				
			} else
				toExecute = command;
			
			final DelegatingScript commandScript = (DelegatingScript) shell.parse(toExecute);
			commandScript.setDelegate(model);
			
			final Object returnValue = commandScript.run();
			
			if (returnValue != null)
				getConsole().println("Return: ", returnValue);
			
		} catch (Throwable t) {
			getConsole().println(t);
		}
	}
	
	protected CompilerConfiguration getCompilerConfig() {
		
		final CompilerConfiguration config = new CompilerConfiguration();
		config.setScriptBaseClass(DelegatingScript.class.getName());
		return config;
	}
}
