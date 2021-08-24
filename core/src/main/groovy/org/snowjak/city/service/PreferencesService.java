/**
 * 
 */
package org.snowjak.city.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.snowjak.city.configuration.Configuration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.github.czyzby.autumn.annotation.Component;

/**
 * Provides {@link ScopedPreferences} instances from the application's
 * preferences-file.
 * 
 * @author snowjak88
 *
 */
@Component
public class PreferencesService {
	
	private final Map<String, ScopedPreferences> scopedInstances = Collections.synchronizedMap(new HashMap<>());
	
	/**
	 * Get a {@link ScopedPreferences} object for the given scope-name. The
	 * resulting ScopedPreferences will read/write preferences whose keys are
	 * prefixed with "{@code [name].}".
	 * <p>
	 * Note that, due to the way {@link ScopedPreferences} currently works, it is
	 * very inefficient to work with a ScopedPreferences whose scope is {@code  ""}.
	 * Better to simply refer straight to the {@link Preferences} instance on
	 * {@link Gdx#app Gdx.app.getPreferences} {@code (}
	 * {@link Configuration#PREFERENCES_NAME} {@code )}.
	 * </p>
	 * 
	 * @param name
	 * @return
	 */
	public ScopedPreferences get(String name) {
		
		if (!scopedInstances.containsKey(name))
			synchronized (this) {
				if (!scopedInstances.containsKey(name))
					scopedInstances.put(name,
							new ScopedPreferences(name + ".", Gdx.app.getPreferences(Configuration.PREFERENCES_NAME)));
			}
		
		return scopedInstances.get(name);
	}
	
	/**
	 * Provides a scoped window into a backing {@link Preferences} instance. All
	 * references to this ScopedPreferences have a certain "key" added to them
	 * automatically.
	 * 
	 * @author snowjak88
	 *
	 */
	public static class ScopedPreferences implements Preferences {
		
		private final String scope;
		private final Preferences backing;
		private final Map<String, Object> scratchMap = new LinkedHashMap<>();
		private final List<String> scratchList = new LinkedList<>();
		
		public ScopedPreferences(String scope, Preferences backingPreferences) {
			
			this.scope = scope;
			this.backing = backingPreferences;
		}
		
		@Override
		public Preferences putBoolean(String key, boolean val) {
			
			backing.putBoolean(scope + key, val);
			return this;
		}
		
		@Override
		public Preferences putInteger(String key, int val) {
			
			backing.putInteger(scope + key, val);
			return this;
		}
		
		@Override
		public Preferences putLong(String key, long val) {
			
			backing.putLong(scope + key, val);
			return this;
		}
		
		@Override
		public Preferences putFloat(String key, float val) {
			
			backing.putFloat(scope + key, val);
			return this;
		}
		
		@Override
		public Preferences putString(String key, String val) {
			
			backing.putString(scope + key, val);
			return this;
		}
		
		@Override
		public Preferences put(Map<String, ?> vals) {
			
			scratchMap.clear();
			for (String key : vals.keySet())
				scratchMap.put(scope + key, (Object) vals.get(key));
			backing.put(scratchMap);
			
			return this;
		}
		
		@Override
		public boolean getBoolean(String key) {
			
			return backing.getBoolean(scope + key);
		}
		
		@Override
		public int getInteger(String key) {
			
			return backing.getInteger(scope + key);
		}
		
		@Override
		public long getLong(String key) {
			
			return backing.getLong(scope + key);
		}
		
		@Override
		public float getFloat(String key) {
			
			return backing.getFloat(scope + key);
		}
		
		@Override
		public String getString(String key) {
			
			return backing.getString(scope + key);
		}
		
		@Override
		public boolean getBoolean(String key, boolean defValue) {
			
			return backing.getBoolean(scope + key, defValue);
		}
		
		@Override
		public int getInteger(String key, int defValue) {
			
			return backing.getInteger(scope + key, defValue);
		}
		
		@Override
		public long getLong(String key, long defValue) {
			
			return backing.getLong(scope + key, defValue);
		}
		
		@Override
		public float getFloat(String key, float defValue) {
			
			return backing.getFloat(scope + key, defValue);
		}
		
		@Override
		public String getString(String key, String defValue) {
			
			return backing.getString(scope + key, defValue);
		}
		
		@Override
		public Map<String, ?> get() {
			
			scratchMap.clear();
			
			@SuppressWarnings("unchecked")
			final Map<String, Object> backingMap = (Map<String, Object>) backing.get();
			for (String key : backingMap.keySet())
				if (key.startsWith(scope))
					scratchMap.put(key.replaceFirst(scope, ""), backingMap.get(key));
				
			return Collections.unmodifiableMap(scratchMap);
		}
		
		@Override
		public boolean contains(String key) {
			
			return backing.contains(scope + key);
		}
		
		@Override
		public void clear() {
			
			scratchList.clear();
			for (String key : backing.get().keySet())
				if (key.startsWith(scope))
					scratchList.add(key);
				
			scratchList.forEach(k -> backing.remove(k));
		}
		
		@Override
		public void remove(String key) {
			
			backing.remove(scope + key);
		}
		
		@Override
		public void flush() {
			
			backing.flush();
		}
	}
}
