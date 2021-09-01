package org.snowjak.city.configuration.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines a single asset inside of an @{@link AssetPreload} annotation.
 * 
 * @author snowjak88
 *
 */
@Documented
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface AssetPreloadItem {
	
	String value();
	
	Class<?> type();
}
