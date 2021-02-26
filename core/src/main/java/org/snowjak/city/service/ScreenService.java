/**
 * 
 */
package org.snowjak.city.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.snowjak.city.screen.AbstractScreen;
import org.snowjak.city.screen.ScreenRegistrationProcessor;

import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

/**
 * Functions as a registrar for {@link AbstractScreen} implementations.
 * 
 * @author snowjak88
 *
 */
@Component
public class ScreenService {
	
	private static final Logger LOG = LoggerService.forClass(ScreenService.class);
	
	@Inject
	private ScreenRegistrationProcessor screenRegistrationProcessor;
	
	private final Map<Class<? extends AbstractScreen>, AbstractScreen> screensByType = new HashMap<>();
	private final Map<String, AbstractScreen> screensBySimpleName = new HashMap<>();
	
	@Initiate
	public void initiate() {
		
		LOG.debug("Initiating AbstractScreen registration.");
		screenRegistrationProcessor.getFoundInstances().forEach(this::registerScreen);
		LOG.debug("AbstractScreen registration completed.");
	}
	
	public <T extends AbstractScreen> void registerScreen(Class<T> clazz, T instance) {
		
		LOG.debug("Registering AbstractScreen instance of type [{0}]", clazz.getName());
		
		screensByType.put(clazz, instance);
		screensBySimpleName.put(clazz.getSimpleName(), instance);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractScreen> T getScreen(Class<T> clazz) {
		
		return (T) screensByType.getOrDefault(clazz, null);
	}
	
	public AbstractScreen getScreen(String simpleClassName) {
		
		return screensBySimpleName.getOrDefault(simpleClassName, null);
	}
	
	public Set<String> getScreenNames() {
		
		return screensBySimpleName.keySet();
	}
}
