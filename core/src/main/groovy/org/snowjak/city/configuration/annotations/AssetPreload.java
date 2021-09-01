package org.snowjak.city.configuration.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.snowjak.city.service.GameAssetService;

/**
 * Indicates that a type knows that certain assets will need to be pre-loaded by
 * the {@link GameAssetService}.
 * 
 * @author snowjak88
 *
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface AssetPreload {
	
	AssetPreloadItem[] value();
}
