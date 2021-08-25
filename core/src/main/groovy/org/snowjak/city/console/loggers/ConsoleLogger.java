/**
 * 
 */
package org.snowjak.city.console.loggers;

import java.util.List;

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
	
	private final List<Object[]> bufferedLogEntries;
	
	public ConsoleLogger(Console console, LoggerService service, List<Object[]> bufferedLogEntries, Class<?> forClass) {
		
		super(service, forClass);
		this.bufferedLogEntries = bufferedLogEntries;
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
			bufferedLogEntries.add(new Object[] { tag + ": " + message });
	}
	
	@Override
	protected void logDebug(String tag, String message, Throwable exception) {
		
		if (console != null && console.isReady()) {
			printStream.println(tag + ": " + message);
			printStream.println(exception.getClass().getName() + ": " + exception.getMessage());
		} else {
			bufferedLogEntries.add(new Object[] { tag + ": " + message });
			bufferedLogEntries.add(new Object[] { exception.getClass().getName() + ": " + exception.getMessage() });
		}
	}
	
	@Override
	protected void logInfo(String tag, String message) {
		
		if (console != null && console.isReady())
			printStream.println(tag + ": " + message);
		else
			bufferedLogEntries.add(new Object[] { tag + ": " + message });
	}
	
	@Override
	protected void logInfo(String tag, String message, Throwable exception) {
		
		if (console != null && console.isReady()) {
			printStream.println(tag + ": " + message);
			printStream.println(exception.getClass().getName() + ": " + exception.getMessage());
		} else {
			bufferedLogEntries.add(new Object[] { tag + ": " + message });
			bufferedLogEntries.add(new Object[] { exception.getClass().getName() + ": " + exception.getMessage() });
		}
	}
	
	@Override
	protected void logError(String tag, String message) {
		
		if (console != null && console.isReady())
			printStream.println(tag + ": " + message);
		else
			bufferedLogEntries.add(new Object[] { tag + ": " + message });
	}
	
	@Override
	protected void logError(String tag, String message, Throwable exception) {
		
		if (console != null && console.isReady()) {
			printStream.println(tag + ": " + message);
			printStream.println(exception.getClass().getName() + ": " + exception.getMessage());
		} else {
			bufferedLogEntries.add(new Object[] { tag + ": " + message });
			bufferedLogEntries.add(new Object[] { exception.getClass().getName() + ": " + exception.getMessage() });
		}
	}
}
