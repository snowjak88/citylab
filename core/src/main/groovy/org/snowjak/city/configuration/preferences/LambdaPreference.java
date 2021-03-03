/**
 * 
 */
package org.snowjak.city.configuration.preferences;

import java.util.function.Function;
import java.util.function.Supplier;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.github.czyzby.autumn.mvc.component.preferences.dto.AbstractPreference;

/**
 * Allows you to specify lambda-expressions for preferences-handling.
 * 
 * @author snowjak88
 *
 */
public class LambdaPreference<T> extends AbstractPreference<T> {
	
	private final Supplier<T> defaultValue;
	private final Function<Actor, T> extractFromActor;
	private final Function<String, T> convert;
	private final Function<T, String> serialize;
	
	public LambdaPreference(Supplier<T> defaultValue, Function<Actor, T> extractFromActor, Function<String, T> convert,
			Function<T, String> serialize) {
		
		this.defaultValue = defaultValue;
		this.extractFromActor = extractFromActor;
		this.convert = convert;
		this.serialize = serialize;
	}
	
	@Override
	public T getDefault() {
		
		if (defaultValue == null)
			return null;
		
		return defaultValue.get();
	}
	
	@Override
	public T extractFromActor(Actor actor) {
		
		if (extractFromActor == null)
			return null;
		
		return extractFromActor.apply(actor);
	}
	
	@Override
	protected T convert(String rawPreference) {
		
		if (convert == null)
			return null;
		
		return convert.apply(rawPreference);
	}
	
	@Override
	protected String serialize(T preference) {
		
		if (serialize == null)
			return null;
		
		return serialize.apply(preference);
	}
	
}
