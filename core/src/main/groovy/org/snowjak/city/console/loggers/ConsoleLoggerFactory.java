/**
 * 
 */
package org.snowjak.city.console.loggers;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.snowjak.city.console.Console;

import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerFactory;
import com.github.czyzby.kiwi.log.LoggerService;

/**
 * {@link LoggerFactory} for producing {@link ConsoleLogger} instances.
 * 
 * @author snowjak88
 *
 */
public class ConsoleLoggerFactory implements LoggerFactory {
	
	private static final ConsoleLoggerFactory INSTANCE = new ConsoleLoggerFactory();
	
	public static ConsoleLoggerFactory get() {
		
		return INSTANCE;
	}
	
	private final List<Object[]> bufferedLogEntries = Collections.synchronizedList(new LinkedList<>());
	private final Map<Class<?>, ConsoleLogger> loggers = Collections.synchronizedMap(new LinkedHashMap<>());
	
	private Console console = null;
	
	private ConsoleLoggerFactory() {
		
	}
	
	public void setConsole(Console console) {
		
		this.console = console;
		for (ConsoleLogger logger : loggers.values())
			logger.setConsole(console);
		
		console.addOnReadyAction(() -> bufferedLogEntries.forEach(l -> console.println(l)));
	}
	
	@Override
	public Logger newLogger(LoggerService service, Class<?> forClass) {
		
		return loggers.computeIfAbsent(forClass,
				(c) -> new ConsoleLogger(console, service, bufferedLogEntries, forClass, true));
	}
	
}
