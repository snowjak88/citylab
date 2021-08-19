/**
 * 
 */
package org.snowjak.city.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.snowjak.city.console.loggers.ConsoleLoggerFactory;

import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerFactory;
import com.github.czyzby.kiwi.log.impl.AsynchronousLogger.AsynchronousLoggerFactory;

/**
 * Rewrite of the {@link com.github.czyzby.kiwi.log.LoggerService LoggerService
 * from Kiwi}, to permit us to use our own {@link ConsoleLoggerFactory} as the
 * default {@link LoggerFactory}.
 * <p>
 * This wouldn't be necessary if Kiwi's LoggerService performed its own
 * LoggerFactory accessing through a getter. Then, we could get the behavior we
 * want by extending LoggerService and overriding the one {@code getFactory()}
 * method.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class LoggerService extends com.github.czyzby.kiwi.log.LoggerService {
	
	private static final LoggerService INSTANCE = new LoggerService();
	
	private final Map<Class<?>, Logger> loggers = Collections.synchronizedMap(new LinkedHashMap<>());
	private LoggerFactory loggerFactory = ConsoleLoggerFactory.get();
	
	private volatile AsyncExecutor executor;
	
	public static Logger forClass(Class<?> forClass) {
		
		return INSTANCE.getLoggerForClass(forClass);
	}
	
	private LoggerService() {
		
		super();
		setUseSimpleClassNames(true);
	}
	
	@Override
	public Logger getLoggerForClass(Class<?> forClass) {
		
		return loggers.computeIfAbsent(forClass, (c) -> getFactory().newLogger(this, forClass));
	}
	
	@Override
	public AsyncExecutor getExecutor() {
		
		if (executor == null)
			synchronized (this) {
				if (executor == null)
					executor = new AsyncExecutor(1);
			}
		
		return executor;
	}
	
	@Override
	public LoggerFactory getFactory() {
		
		return loggerFactory;
	}
	
	@Override
	public void setFactory(LoggerFactory factory) {
		
		this.loggerFactory = factory;
	}
	
	@Override
	public void setUseAsynchronousLoggers(boolean useAsynchronousLoggers) {
		
		setFactory(useAsynchronousLoggers ? new AsynchronousLoggerFactory() : ConsoleLoggerFactory.get());
	}
}
