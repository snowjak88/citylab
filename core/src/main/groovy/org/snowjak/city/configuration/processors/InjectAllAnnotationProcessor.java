/**
 * 
 */
package org.snowjak.city.configuration.processors;

import java.util.Collection;
import java.util.LinkedList;

import org.snowjak.city.configuration.annotations.InjectAll;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.github.czyzby.autumn.context.Context;
import com.github.czyzby.autumn.context.ContextDestroyer;
import com.github.czyzby.autumn.context.ContextInitializer;
import com.github.czyzby.autumn.context.error.ContextInitiationException;
import com.github.czyzby.autumn.processor.AbstractAnnotationProcessor;

/**
 * Processes @{@link InjectAll}-annotated fields.
 * 
 * @author snowjak88
 *
 */
public class InjectAllAnnotationProcessor extends AbstractAnnotationProcessor<InjectAll> {
	
	@Override
	public boolean isSupportingFields() {
		
		return true;
	}
	
	@Override
	public void processField(Field field, InjectAll annotation, Object component, Context context,
			ContextInitializer initializer, ContextDestroyer contextDestroyer) {
		
		final boolean isACollection = Collection.class.isAssignableFrom(field.getType());
		final boolean isAnArray = Array.class.isAssignableFrom(field.getType());
		if (!isACollection && !isAnArray)
			throw new ContextInitiationException("Cannot inject all instances of [" + annotation.value().getName()
					+ "] into [" + component.getClass().getName() + "].[" + field.getName()
					+ "] -- must be one of { Collection, Array }!");
		
		processRegularInjection(field, annotation, component, context);
	}
	
	protected void processRegularInjection(final Field field, final InjectAll annotation, final Object component,
			final Context context) {
		
		final Class<?> dependencyClass = annotation.value();
		setFieldValue(field, component, context.getAll(dependencyClass));
	}
	
	protected void setFieldValue(final Field field, final Object component, final Array<Object> values) {
		
		try {
			if (Collection.class.isAssignableFrom(field.getType()))
				setAsCollection(field, component, values);
			
			else if (Array.class.isAssignableFrom(field.getType()))
				setAsArray(field, component, values);
			
		} catch (final ReflectionException exception) {
			throw new ContextInitiationException(
					"Unable to inject value of field: " + field + " into component: " + component, exception);
		}
	}
	
	protected void setAsCollection(final Field field, final Object component, final Array<Object> values)
			throws ReflectionException {
		
		field.setAccessible(true);
		
		if (field.get(component) == null)
			field.set(component, new LinkedList<>());
		
		@SuppressWarnings("unchecked")
		final Collection<Object> c = (Collection<Object>) field.get(component);
		values.forEach(v -> c.add(v));
	}
	
	protected void setAsArray(final Field field, final Object component, final Array<Object> values)
			throws ReflectionException {
		
		field.setAccessible(true);
		
		if (field.get(component) == null)
			field.set(component, new Array<>());
		
		@SuppressWarnings("unchecked")
		final Array<Object> a = (Array<Object>) field.get(component);
		values.forEach(v -> a.add(v));
	}
	
	@Override
	public Class<InjectAll> getSupportedAnnotationType() {
		
		return InjectAll.class;
	}
	
}
