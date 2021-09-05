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
import com.google.common.base.Throwables;

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
	private final boolean logToDefault;
	
	public ConsoleLogger(Console console, LoggerService service, List<Object[]> bufferedLogEntries, Class<?> forClass, boolean logToDefault) {
		
		super(service, forClass);
		this.bufferedLogEntries = bufferedLogEntries;
		this.logToDefault = logToDefault;
		setConsole(console);
	}
	
	public void setConsole(Console console) {
		
		this.console = console;
		
		if (this.console != null)
			this.printStream = new ConsolePrintStream(console);
	}
	
	@Override
	protected void logDebug(String tag, String message) {
		if(logToDefault)
		super.logDebug(tag, message);
		
		if (console != null && console.isReady())
			printStream.println(tag + ": " + message);
		else
			bufferedLogEntries.add(new Object[] { tag + ": " + message });
	}
	
	@Override
	protected void logDebug(String tag, String message, Throwable exception) {
		if(logToDefault)
		super.logDebug(tag, message, exception);
		
		if (console != null && console.isReady()) {
			printStream.println(tag + ": " + message);
			printStream.println(exception.getClass().getName() + ": " + exception.getMessage());
			printStream.println(Throwables.getStackTraceAsString(exception));
		} else {
			bufferedLogEntries.add(new Object[] { tag + ": " + message });
			bufferedLogEntries.add(new Object[] { exception.getClass().getName() + ": " + exception.getMessage() });
			bufferedLogEntries.add(new Object[] { Throwables.getStackTraceAsString(exception) });
		}
	}
	
	@Override
	protected void logInfo(String tag, String message) {
		super.logInfo(tag, message);
		if (console != null && console.isReady())
			printStream.println(tag + ": " + message);
		else
			bufferedLogEntries.add(new Object[] { tag + ": " + message });
	}
	
	@Override
	protected void logInfo(String tag, String message, Throwable exception) {
		if(logToDefault)
		super.logInfo(tag, message, exception);
		
		if (console != null && console.isReady()) {
			printStream.println(tag + ": " + message);
			printStream.println(exception.getClass().getName() + ": " + exception.getMessage());
			printStream.println(Throwables.getStackTraceAsString(exception));
		} else {
			bufferedLogEntries.add(new Object[] { tag + ": " + message });
			bufferedLogEntries.add(new Object[] { exception.getClass().getName() + ": " + exception.getMessage() });
			bufferedLogEntries.add(new Object[] { Throwables.getStackTraceAsString(exception) });
		}
	}
	
	@Override
	protected void logError(String tag, String message) {
		if(logToDefault)
		super.logError(tag, message);
		
		if (console != null && console.isReady())
			printStream.println(tag + ": " + message);
		else
			bufferedLogEntries.add(new Object[] { tag + ": " + message });
	}
	
	@Override
	protected void logError(String tag, String message, Throwable exception) {
		
		if(logToDefault)
			super.logError(tag, message, exception);
		
		if (console != null && console.isReady()) {
			printStream.println(tag + ": " + message);
			printStream.println(exception.getClass().getName() + ": " + exception.getMessage());
			printStream.println(Throwables.getStackTraceAsString(exception));
		} else {
			bufferedLogEntries.add(new Object[] { tag + ": " + message });
			bufferedLogEntries.add(new Object[] { exception.getClass().getName() + ": " + exception.getMessage() });
			bufferedLogEntries.add(new Object[] { Throwables.getStackTraceAsString(exception) });
		}
	}
}
