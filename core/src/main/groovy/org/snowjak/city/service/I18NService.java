/**
 * 
 */
package org.snowjak.city.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import org.snowjak.city.CityGame;
import org.snowjak.city.configuration.InitPriority;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.I18NBundleLoader.I18NBundleParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;

/**
 * Provides message-resolution against I18N bundles. Handles loading I18N
 * bundles, both on application-load and on Locale-switching.
 * <p>
 * Bundles come in 2 varieties:
 * <ol>
 * <li><em>Internal</em> -- shipped with the application. There's only 1 of
 * these.</li>
 * <li><em>External</em> -- loaded at run-time. Typically defined by a Module
 * for its own internal use.</li>
 * </ol>
 * </p>
 * <p>
 * Only the Internal bundle is loaded automatically. External bundles must be
 * explicitly loaded, with a call to
 * </p>
 * 
 * @author snowjak88
 *
 */
@Component
public class I18NService {
	
	private Locale currentLocale;
	private FileHandle internalBundleBase;
	
	private I18NBundle baseBundle;
	
	private final Map<String, I18NBundleContext> contexts = new LinkedHashMap<>();
	private final Map<String, Set<I18NBundle>> cachedBundles = new LinkedHashMap<>();
	private final Map<String, Set<FileHandle>> configuredBundleBases = new LinkedHashMap<>();
	private final GameAssetService assetService;
	
	public I18NService(GameAssetService assetService) {
		
		this.assetService = assetService;
		
		internalBundleBase = Gdx.files.internal(CityGame.INTERNAL_BUNDLE_BASE);
		
		setLocale(Locale.getDefault());
	}
	
	/**
	 * Get a message from the Internal bundle.
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key) {
		
		synchronized (this) {
			if (baseBundle == null) {
				assetService.finishLoading(internalBundleBase.path());
				baseBundle = assetService.get(internalBundleBase.path(), I18NBundle.class);
			}
			
			try {
				return baseBundle.get(key);
			} catch (MissingResourceException e) {
				return "???" + key + "???";
			}
		}
	}
	
	private String get(String contextName, String key) {
		
		synchronized (this) {
			//
			// Attempt to resolve this message on one of the BundleContext's configured
			// bundles.
			for (I18NBundle bundle : getBundles(contextName)) {
				try {
					return bundle.get(key);
				} catch (MissingResourceException e) {
					//
					// move on to the next bundle
				}
			}
			
			//
			// As a fallback: attempt to get the message from the Internal bundle.
			return get(key);
		}
	}
	
	/**
	 * Get a message from the Internal bundle, inserting arguments.
	 * 
	 * @param key
	 * @param arguments
	 * @return
	 */
	public String format(String key, Object... arguments) {
		
		synchronized (this) {
			if (baseBundle == null) {
				assetService.finishLoading(internalBundleBase.path());
				baseBundle = assetService.get(internalBundleBase.path(), I18NBundle.class);
			}
			
			try {
				return baseBundle.format(key, arguments);
			} catch (MissingResourceException e) {
				return "???" + key + "???";
			}
		}
	}
	
	private String format(String contextName, String key, Object... arguments) {
		
		synchronized (this) {
			//
			// Attempt to resolve this message on one of the BundleContext's configured
			// bundles.
			for (I18NBundle bundle : getBundles(contextName)) {
				try {
					return bundle.format(key, arguments);
				} catch (MissingResourceException e) {
					//
					// move on to the next bundle
				}
			}
			
			//
			// As a fallback: attempt to get the message from the Internal bundle.
			return format(key, arguments);
		}
	}
	
	/**
	 * Get the {@link I18NBundleContext context} identified by the given name and
	 * using the given {@code baseDirectory} for filename-resolution.
	 * <p>
	 * If a context identified by the given name already exists -- even if it uses a
	 * different base-directory -- then the existing context is returned and
	 * {@code baseDirectory} is ignored.
	 * </p>
	 * 
	 * @param contextName
	 * @return
	 */
	public I18NBundleContext getContext(String contextName, FileHandle baseDirectory) {
		
		synchronized (this) {
			return contexts.computeIfAbsent(contextName,
					(n) -> new I18NBundleContext(this, contextName, baseDirectory));
		}
	}
	
	@Initiate(priority = InitPriority.VERY_HIGH_PRIORITY)
	public void init() {
		
		// internalBundleBase = Gdx.files.internal(CityGame.INTERNAL_BUNDLE_BASE);
		//
		// setLocale(Locale.getDefault());
	}
	
	public void setLocale(Locale locale) {
		
		synchronized (this) {
			this.currentLocale = locale;
			
			cachedBundles.clear();
			
			reloadInternalBundle();
			reloadExternalBundles();
		}
	}
	
	private void reloadInternalBundle() {
		
		synchronized (this) {
			if (assetService.isLoaded(internalBundleBase.path(), I18NBundle.class))
				assetService.unload(internalBundleBase.path(), I18NBundle.class);
			assetService.load(internalBundleBase.path(), I18NBundle.class, new I18NBundleParameter(currentLocale));
		}
	}
	
	private void reloadExternalBundles() {
		
		synchronized (this) {
			for (String contextName : configuredBundleBases.keySet())
				for (FileHandle bundleBase : getBundleBases(contextName)) {
					if (assetService.isLoaded(bundleBase.path(), I18NBundle.class))
						assetService.unload(bundleBase.path(), I18NBundle.class);
					assetService.load(bundleBase.path(), I18NBundle.class, new I18NBundleParameter(currentLocale));
				}
		}
	}
	
	private void addBundle(String contextName, FileHandle bundleBase) {
		
		synchronized (this) {
			getBundleBases(contextName).add(bundleBase);
			assetService.load(bundleBase.path(), I18NBundle.class, new I18NBundleParameter(currentLocale));
		}
	}
	
	/**
	 * Get the set of cached bundles in the parent I18NService. Refresh that cache
	 * if necessary.
	 * 
	 * @return
	 */
	private Set<I18NBundle> getBundles(String contextName) {
		
		synchronized (this) {
			if (cachedBundles.computeIfAbsent(contextName, (n) -> new LinkedHashSet<>()).isEmpty()) {
				for (FileHandle bundleBase : getBundleBases(contextName)) {
					assetService.finishLoading(bundleBase.path());
					final I18NBundle loadedBundle = assetService.get(bundleBase.path(), I18NBundle.class);
					cachedBundles.get(contextName).add(loadedBundle);
				}
			}
			
			return cachedBundles.get(contextName);
		}
	}
	
	/**
	 * Get the set of configured bundle-bases for any given context-name.
	 * 
	 * @param contextName
	 * @return
	 */
	private Set<FileHandle> getBundleBases(String contextName) {
		
		synchronized (this) {
			return configuredBundleBases.computeIfAbsent(contextName, (n) -> new LinkedHashSet<>());
		}
	}
	
	/**
	 * Represents a scoped set of I18N bundles. Permits message-resolution to
	 * fall-back to the Internal bundle.
	 * 
	 * @author snowjak88
	 *
	 */
	public static class I18NBundleContext {
		
		private final String contextName;
		private final FileHandle baseDirectory;
		private final I18NService service;
		
		private I18NBundleContext(I18NService service, String contextName, FileHandle baseDirectory) {
			
			this.contextName = contextName;
			this.service = service;
			this.baseDirectory = baseDirectory;
		}
		
		public void addBundle(String bundleBaseName) {
			
			addBundle(baseDirectory.child(bundleBaseName));
		}
		
		public void addBundle(FileHandle bundleBase) {
			
			service.addBundle(contextName, bundleBase);
		}
		
		/**
		 * Get a message from this bundle-context. If the given key is not found in any
		 * of this context's configured bundles, then uses the Internal bundle as a
		 * fall-back.
		 * 
		 * @param key
		 * @return
		 */
		public String get(String key) {
			
			return service.get(contextName, key);
		}
		
		/**
		 * Get a message from this bundle-context, inserting arguments. If the given key
		 * is not found in any of this context's configured bundles, then uses the
		 * Internal bundle as a fall-back.
		 * 
		 * @param key
		 * @return
		 */
		public String format(String key, Object... arguments) {
			
			return service.format(contextName, key, arguments);
		}
		
		public Set<FileHandle> getBundles() {
			
			return service.getBundleBases(contextName);
		}
	}
	
	/**
	 * Pretends to be an I18NBundleContext, but really just snoops on what bundles
	 * you add.
	 * 
	 * @author snowjak88
	 *
	 */
	public static class ProxiedI18NBundleContext extends I18NBundleContext {
		
		private final FileHandle baseDirectory;
		private final Set<FileHandle> bundleBases = new LinkedHashSet<>();
		
		public ProxiedI18NBundleContext(FileHandle baseDirectory) {
			
			super(null, null, null);
			this.baseDirectory = baseDirectory;
		}
		
		@Override
		public void addBundle(String bundleBaseName) {
			
			addBundle(baseDirectory.child(bundleBaseName));
		}
		
		@Override
		public void addBundle(FileHandle bundleBase) {
			
			bundleBases.add(bundleBase);
		}
		
		@Override
		public Set<FileHandle> getBundles() {
			
			return bundleBases;
		}
		
		@Override
		public String get(String key) {
			
			return key;
		}
		
		@Override
		public String format(String key, Object... arguments) {
			
			return key;
		}
	}
}
