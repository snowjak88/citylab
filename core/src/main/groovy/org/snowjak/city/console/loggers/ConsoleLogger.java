/**
 * 
 */
package org.snowjak.city.console.loggers;

import org.snowjak.city.console.Console;
import org.snowjak.city.console.ConsolePrintStream;

import com.badlogic.gdx.Application;
import com.github.czyzby.kiwi.log.LoggerService;
import com.github.czyzby.kiwi.log.impl.DefaultLogger;

/**
 * Delegate logger, writes to the active {@link Console} (with
 * {@link Application Gdx.app} as a fallback).
 * 
 * @author snowjak88
 *
 */
public class ConsoleLogger extends DefaultLogger {
	
	private Console console;
	private ConsolePrintStream printStream;
	
	public ConsoleLogger(Console console, LoggerService service, Class<?> forClass) {
		
		super(service, forClass);
		
		setConsole(console);
	}
	
	public void setConsole(Console console) {
		
		this.console = console;
		
		if (this.console != null)
			this.printStream = new ConsolePrintStream(console);
	}
	
	@Override
	protected void logDebug(String tag, String message) {
		
		if (console != null && console.isReady())
			printStream.println(tag + ": " + message);
		else
			super.logDebug(tag, message);
	}
	
	@Override
	protected void logDebug(String tag, String message, Throwable exception) {
		
		if (console != null && console.isReady()) {
			printStream.println(tag + ": " + message);
			printStream.println(exception.getClass().getName() + ": " + exception.getMessage());
		} else
			super.logDebug(tag, message, exception);
	}
	
	@Override
	protected void logInfo(String tag, String message) {
		
		if (console != null && console.isReady())
			printStream.println(tag + ": " + message);
		else
			super.logInfo(tag, message);
	}
	
	@Override
	protected void logInfo(String tag, String message, Throwable exception) {
		
		if (console != null && console.isReady()) {
			printStream.println(tag + ": " + message);
			printStream.println(exception.getClass().getName() + ": " + exception.getMessage());
		} else
			super.logInfo(tag, message, exception);
	}
	
	@Override
	protected void logError(String tag, String message) {
		
		if (console != null && console.isReady())
			printStream.println(tag + ": " + message);
		else
			super.logError(tag, message);
	}
	
	@Override
	protected void logError(String tag, String message, Throwable exception) {
		
		if (console != null && console.isReady()) {
			printStream.println(tag + ": " + message);
			printStream.println(exception.getClass().getName() + ": " + exception.getMessage());
		} else
			super.logError(tag, message, exception);
	}
}
