/**
 * 
 */
package org.snowjak.city.screen;

import java.util.HashMap;
import java.util.Map;

import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Processor;
import com.github.czyzby.autumn.context.Context;
import com.github.czyzby.autumn.context.ContextDestroyer;
import com.github.czyzby.autumn.context.ContextInitializer;
import com.github.czyzby.autumn.processor.AbstractAnnotationProcessor;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

/**
 * @author snowjak88
 *
 */
@Processor
public class ScreenRegistrationProcessor extends AbstractAnnotationProcessor<Component> {
	
	private static final Logger LOG = LoggerService.forClass(ScreenRegistrationProcessor.class);
	
	private final Map<Class<AbstractScreen>, AbstractScreen> instances = new HashMap<>();
	
	@Override
	public Class<Component> getSupportedAnnotationType() {
		
		return Component.class;
	}
	
	@Override
	public boolean isSupportingTypes() {
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void processType(Class<?> type, Component annotation, Object component, Context context,
			ContextInitializer initializer, ContextDestroyer contextDestroyer) {
		
		if (!(component instanceof AbstractScreen))
			return;
		
		LOG.debug("Recording AbstractScreen instance of type [{0}]", type.getName());
		instances.put((Class<AbstractScreen>) type, (AbstractScreen) component);
	}
	
	public Map<Class<AbstractScreen>, AbstractScreen> getFoundInstances() {
		
		return instances;
	}
}
