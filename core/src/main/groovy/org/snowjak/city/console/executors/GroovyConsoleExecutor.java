/**
* 
*/
package org.snowjak.city.console.executors;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.snowjak.city.console.Console;
import org.snowjak.city.console.ConsolePrintStream;
import org.snowjak.city.console.model.ConsoleModel;
import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.tiles.Tile;
import org.snowjak.city.map.tiles.TileSet;
import org.snowjak.city.module.Module;
import org.snowjak.city.util.Util;

import com.badlogic.gdx.assets.AssetDescriptor;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * @author snowjak88
 *
 */
public class GroovyConsoleExecutor extends AbstractConsoleExecutor {
	
	private final StringBuffer multiLineCommand;
	
	private final ConsoleModel model;
	private final GroovyShell shell;
	
	public GroovyConsoleExecutor(Console console, ConsoleModel model, ConsolePrintStream printStream) {
		
		super(console);
		
		this.multiLineCommand = new StringBuffer();
		
		this.model = model;
		
		//
		// Set up our PrintStream to capture normal output
		model.addProtectedVariable("out", printStream);
		
		shell = new GroovyShell(model, getCompilerConfig());
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
			
			final Script commandScript = shell.parse(toExecute);
			
			final Object returnValue = commandScript.run();
			
			if (returnValue != null)
				getConsole().println(returnValue);
			
		} catch (Throwable t) {
			getConsole().println(t);
		}
	}
	
	protected CompilerConfiguration getCompilerConfig() {
		
		final CompilerConfiguration config = new CompilerConfiguration();
		
		final ImportCustomizer imports = new ImportCustomizer();
		imports.addStarImports("org.snowjak.city.ecs.components");
		imports.addStarImports("com.badlogic.ashley.core");
		imports.addStarImports("com.badlogic.gdx.audio");
		imports.addStarImports("com.badlogic.gdx.files");
		imports.addStarImports("com.badlogic.gdx.graphics");
		imports.addStarImports("com.badlogic.gdx.math");
		imports.addStarImports("com.badlogic.gdx.utils");
		imports.addImports(
				// jCity types
				Module.class.getName(), CityMap.class.getName(), Tile.class.getName(), TileSet.class.getName(),
				Util.class.getName(),
				// Misc. LibGDX types
				AssetDescriptor.class.getName());
		
		config.addCompilationCustomizers(imports);
		return config;
	}
}
