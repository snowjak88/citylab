package org.snowjak.city.configuration.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Denotes that the asset named by this annotation should be scheduled for
 * loading, and the annotated field injected with the loaded asset-object.
 * 
 * @author snowjak88
 *
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface Asset {
	
	/**
	 * The filename of the asset to be injected, relative to the application's
	 * "assets" directory.
	 * 
	 * @return
	 */
	String value();
}
