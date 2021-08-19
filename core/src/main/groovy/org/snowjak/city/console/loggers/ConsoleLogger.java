/**
 * 
 */
package org.snowjak.city.console.loggers;

import org.snowjak.city.console.Console;

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
	
	public ConsoleLogger(Console console, LoggerService service, Class<?> forClass) {
		
		super(service, forClass);
		
		this.console = console;
	}
	
	public void setConsole(Console console) {
		
		this.console = console;
	}
	
	@Override
	protected void logDebug(String tag, String message) {
		
		if (console != null && console.isReady())
			console.print(tag + ": " + message);
		else
			super.logDebug(tag, message);
	}
	
	@Override
	protected void logDebug(String tag, String message, Throwable exception) {
		
		if (console != null && console.isReady()) {
			console.print(tag + ": " + message);
			console.print(exception.getClass().getName() + ": " + exception.getMessage());
		} else
			super.logDebug(tag, message, exception);
	}
	
	@Override
	protected void logInfo(String tag, String message) {
		
		if (console != null && console.isReady())
			console.print(tag + ": " + message);
		else
			super.logInfo(tag, message);
	}
	
	@Override
	protected void logInfo(String tag, String message, Throwable exception) {
		
		if (console != null && console.isReady()) {
			console.print(tag + ": " + message);
			console.print(exception.getClass().getName() + ": " + exception.getMessage());
		} else
			super.logInfo(tag, message, exception);
	}
	
	@Override
	protected void logError(String tag, String message) {
		
		if (console != null && console.isReady())
			console.print(tag + ": " + message);
		else
			super.logError(tag, message);
	}
	
	@Override
	protected void logError(String tag, String message, Throwable exception) {
		
		if (console != null && console.isReady()) {
			console.print(tag + ": " + message);
			console.print(exception.getClass().getName() + ": " + exception.getMessage());
		} else
			super.logError(tag, message, exception);
	}
}
