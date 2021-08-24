/**
 * 
 */
package org.snowjak.city.configuration.processors;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(FIELD)

/**
 * Extension of @{@link Inject}, meant to annotate {@link Collection}s or
 * {@link Array}s, and signifying that you want to receive <em>all</em>
 * context-managed instances of a given type.
 * 
 * @author snowjak88
 *
 */
public @interface InjectAll {
	
	/**
	 * @return the exact class type of injected component.
	 */
	Class<?> value();
}
