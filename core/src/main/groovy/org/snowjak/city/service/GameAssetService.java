/**
 * 
 */
package org.snowjak.city.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.snowjak.city.CityGame;
import org.snowjak.city.resources.ScriptedResource;
import org.snowjak.city.resources.ScriptedResourceLoader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.kiwi.log.Logger;

/**
 * Upgrades the stock {@link AssetManager} to support customized load-failure
 * handling.
 * 
 * @author snowjak88
 *
 */
@Component
public class GameAssetService extends AssetManager {
	
	private static final Logger LOG = LoggerService.forClass(GameAssetService.class);
	
	private final Map<Class<?>, ScriptedResourceLoader<?, ?>> scriptResourceLoaders = new LinkedHashMap<>();
	private final Map<Class<?>, Set<String>> pendingResourceLoads = new LinkedHashMap<>();
	private final AtomicInteger pendingResourceLoadsCount = new AtomicInteger();
	private final Map<Class<?>, Map<String, FileHandle>> scriptedResourceIDs = new LinkedHashMap<>();
	
	private final Map<Class<?>, Map<Class<? extends Throwable>, BiConsumer<AssetDescriptor<?>, Throwable>>> exceptionHandlers = new LinkedHashMap<>();
	private final LinkedList<Runnable> onLoadActions = new LinkedList<>();
	
	private final List<LoadFailureBean> loadFailures = new LinkedList<>();
	
	public GameAssetService() {
		
		super(CityGame.RESOLVER);
	}
	
	/**
	 * Register a new {@link ScriptedResourceLoader} with this asset manager.
	 * 
	 * @param <R>
	 * @param <S>
	 * @param resourceType
	 * @param loader
	 */
	public <R extends ScriptedResource, P extends AssetLoaderParameters<R>, S extends ScriptedResourceLoader<R, P>> void addScriptedResourceLoader(
			Class<R> resourceType, S loader) {
		
		scriptResourceLoaders.put(resourceType, loader);
	}
	
	/**
	 * Register a new exception handler with this asset manager. Assets of the given
	 * type that fail to load properly will trigger the given handler.
	 * 
	 * @param <E>
	 * @param assetType
	 * @param exceptionHandler
	 */
	public <T, E extends Throwable> void addFailureHandler(Class<T> assetType, Class<E> exceptionType,
			BiConsumer<AssetDescriptor<?>, Throwable> exceptionHandler) {
		
		exceptionHandlers.computeIfAbsent(assetType, (a) -> new LinkedHashMap<>()).put(exceptionType, exceptionHandler);
	}
	
	/**
	 * Queue the given asset for loading.
	 * <p>
	 * If the given asset is one of the established {@link ScriptedResource} types,
	 * then some special behavior takes over:
	 * <ol>
	 * <li>This resource is installed into a special "pending" list internally</li>
	 * <li>At the next call to {@link #update()}, this "pending" list is checked. If
	 * the resource's scripted dependencies are all loaded, then this resource is
	 * itself submitted to the AssetManager for loading.</li>
	 * </ol>
	 * </p>
	 */
	@Override
	public synchronized <T> void load(String fileName, Class<T> type) {
		
		if (scriptResourceLoaders.containsKey(type)) {
			pendingResourceLoads.computeIfAbsent(type, (t) -> new LinkedHashSet<>()).add(fileName);
			pendingResourceLoadsCount.incrementAndGet();
		} else
			super.load(fileName, type);
	}
	
	/**
	 * Add an action that will be executed once all queued assets are fully loaded.
	 * 
	 * @param action
	 */
	public void addOnLoadAction(Runnable action) {
		
		if (action != null)
			onLoadActions.add(action);
	}
	
	public float getLoadingProgress() {
		
		return super.getProgress();
	}
	
	@Override
	public synchronized boolean update() {
		
		final boolean isAssetsLoaded = super.update();
		
		final boolean allResourcesLoaded = pendingResourceLoadsCount.get() == 0;
		
		if (isAssetsLoaded)
			updatePendingResourceLoads();
		
		final boolean isComplete = isAssetsLoaded && allResourcesLoaded;
		if (isComplete)
			doOnLoadActions();
		
		return isComplete;
	}
	
	@Override
	public boolean update(int millis) {
		
		final boolean isAssetsLoaded = super.update(millis);
		
		final boolean allResourcesLoaded = pendingResourceLoadsCount.get() == 0;
		
		if (isAssetsLoaded)
			updatePendingResourceLoads();
		
		final boolean isComplete = isAssetsLoaded && allResourcesLoaded;
		if (isComplete)
			doOnLoadActions();
		
		return isComplete;
	}
	
	private boolean updatePendingResourceLoads() {
		
		boolean resourceScheduled = false;
		
		for (Entry<Class<?>, Set<String>> pendingResourceLoad : pendingResourceLoads.entrySet()) {
			final Class<?> pendingType = pendingResourceLoad.getKey();
			final Set<String> fileNames = pendingResourceLoad.getValue();
			
			final Iterator<String> fileNameIterator = fileNames.iterator();
			while (fileNameIterator.hasNext()) {
				
				final String fileName = fileNameIterator.next();
				
				//
				// For the given pending resource, see if it has any resource-dependencies that
				// aren't loaded.
				//
				final ScriptedResourceLoader<?, ?> loader = scriptResourceLoaders.get(pendingType);
				final FileHandle fileHandle = loader.resolve(fileName);
				
				boolean anyNotLoaded = false;
				for (Entry<Class<?>, Set<String>> scriptedDependencies : loader.getScriptedDependencies(fileName)
						.entrySet()) {
					
					final Class<?> dependencyType = scriptedDependencies.getKey();
					for (String dependencyID : scriptedDependencies.getValue()) {
						final FileHandle dependencyFile = scriptedResourceIDs
								.computeIfAbsent(dependencyType, (t) -> new LinkedHashMap<>()).get(dependencyID);
						if (dependencyFile == null || !super.isLoaded(dependencyFile.path(), dependencyType)) {
							anyNotLoaded = true;
							break;
						}
					}
					
					if (anyNotLoaded)
						break;
				}
				
				//
				// If any aren't loaded, then this pending resource can't become non-pending.
				if (anyNotLoaded)
					continue;
					
				//
				// Make sure we can reference this resource by its ID.
				final String resourceID = loader.getResourceID(fileHandle);
				scriptedResourceIDs.computeIfAbsent(pendingType, (t) -> new LinkedHashMap<>()).put(resourceID,
						fileHandle);
				
				//
				// Schedule this resource for loading.
				fileNameIterator.remove();
				pendingResourceLoadsCount.decrementAndGet();
				super.load(fileName, pendingType);
				resourceScheduled = true;
				
			}
		}
		
		return resourceScheduled;
	}
	
	@Override
	public void finishLoading() {
		
		super.finishLoading();
		doOnLoadActions();
	}
	
	public <T> T finishLoading(String assetPath) {
		
		return super.finishLoadingAsset(assetPath);
	}
	
	private void doOnLoadActions() {
		
		while (!onLoadActions.isEmpty())
			onLoadActions.pop().run();
	}
	
	@Override
	protected void taskFailed(@SuppressWarnings("rawtypes") AssetDescriptor assetDesc, RuntimeException ex) {
		
		try {
			LOG.info("Captured load-task failure: {0} while loading {1}", ex.getClass().getSimpleName(),
					assetDesc.fileName);
			
			LOG.info("Drilling down to capture root exception ...");
			Throwable t = ex;
			while (t.getCause() != null) {
				LOG.debug("Drilling down past {0} ...", t.getClass().getSimpleName());
				t = t.getCause();
			}
			LOG.info("Root exception is {0}: {1}", t.getClass().getSimpleName(), t.getMessage());
			
			loadFailures.add(new LoadFailureBean(assetDesc.type, assetDesc.file, t));
			
			if (exceptionHandlers.containsKey(assetDesc.type))
				//
				// We have an exception-handler that matches this asset-type
				handleForAssetType(assetDesc, t, exceptionHandlers.get(assetDesc.type));
			
			else {
				
				final Map<Class<? extends Throwable>, BiConsumer<AssetDescriptor<?>, Throwable>> handlers = exceptionHandlers
						.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(assetDesc.type)).findFirst()
						.map(Entry::getValue).orElse(null);
				
				if (handlers != null)
					//
					// We have an exception handler for a superclass of this asset-type
					handleForAssetType(assetDesc, t, handlers);
				else
					//
					// No valid exception-handlers. Just throw it.
					throw ex;
			}
			
		} catch (Throwable t) {
			throw new GdxRuntimeException("Uh oh -- unhandled resource-loading exception!", t);
		}
	}
	
	private void handleForAssetType(@SuppressWarnings("rawtypes") AssetDescriptor assetDesc, Throwable t,
			Map<Class<? extends Throwable>, BiConsumer<AssetDescriptor<?>, Throwable>> exceptionHandlers)
			throws Throwable {
		
		//
		// Look for exception-handlers that match the exception-class
		if (exceptionHandlers.containsKey(t.getClass()))
			exceptionHandlers.get(t.getClass()).accept(assetDesc, t);
			
		//
		// Look for the first exception-handler that handles a superclass of this
		// exception-class
		final BiConsumer<AssetDescriptor<?>, Throwable> exceptionHandler = exceptionHandlers.entrySet().stream()
				.filter(e -> e.getKey().isAssignableFrom(t.getClass())).findFirst().map(Entry::getValue).orElse(null);
		
		if (exceptionHandler != null)
			exceptionHandler.accept(assetDesc, t);
		else
			throw t;
	}
	
	/**
	 * @return the list of those resources that failed to load, along with their
	 *         failure-reasons
	 */
	public Collection<LoadFailureBean> getLoadFailures() {
		
		return Collections.unmodifiableCollection(loadFailures);
	}
	
	public static class LoadFailureBean {
		
		private final Class<?> assetType;
		private final FileHandle file;
		private final Throwable exception;
		
		public LoadFailureBean(Class<?> assetType, FileHandle file, Throwable exception) {
			
			this.assetType = assetType;
			this.file = file;
			this.exception = exception;
		}
		
		public Class<?> getAssetType() {
			
			return assetType;
		}
		
		public FileHandle getFile() {
			
			return file;
		}
		
		public Throwable getException() {
			
			return exception;
		}
	}
}
