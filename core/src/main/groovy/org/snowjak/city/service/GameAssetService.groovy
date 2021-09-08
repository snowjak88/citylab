/**
 * 
 */
package org.snowjak.city.service

import java.util.Map.Entry
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BiConsumer

import org.apache.groovy.util.Maps
import org.snowjak.city.configuration.MatchingFileHandleResolver
import org.snowjak.city.resources.ScriptedResource
import org.snowjak.city.resources.ScriptedResourceLoader

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.AssetLoader
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.GdxRuntimeException
import com.github.czyzby.autumn.annotation.Component
import com.github.czyzby.kiwi.log.Logger

/**
 * Upgrades the stock {@link AssetManager} to support:
 * <ul>
 * <li>customized load-failure handling by
 * {@link #addFailureHandler(Class, Class, BiConsumer) registering load-failure
 * handlers}</li>
 * <li>Support for loading {@link ScriptedResource}s in order of their
 * dependencies</li>
 * <li>Support for referencing loaded {@link ScriptedResource}s by their
 * {@link ScriptedResource#getId() ID}s</li>
 * </ul>
 * 
 * @author snowjak88
 *
 */
@Component
public class GameAssetService extends AssetManager {
	
	public static final FileHandleResolver FILE_HANDLE_RESOLVER = new MatchingFileHandleResolver(Maps.of("^/?data/.*", new LocalFileHandleResolver()),
	new InternalFileHandleResolver());
	
	private static final Logger LOG = LoggerService.forClass(GameAssetService.class)
	
	/**
	 * Registered {@link ScriptedResourceLoader}s, by resource-type
	 */
	private final Map<Class<?>, ScriptedResourceLoader<?, ?>> scriptResourceLoaders = new LinkedHashMap<>()
	
	/**
	 * Pending {@link ScriptedResource} file-names, by resource-type
	 */
	private final Map<Class<?>, Set<String>> pendingResourceLoads = new LinkedHashMap<>()
	
	/**
	 * {@link #pendingResourceLoads} that were identified as failed, and should be cleaned up
	 */
	private final Map<Class<?>, Set<String>> failedPendingResourceLoads = new LinkedHashMap<>()
	
	/**
	 * Should be equal to the size of {@link #pendingResourceLoads}
	 */
	private final AtomicInteger pendingResourceLoadsCount = new AtomicInteger()
	
	/**
	 * {@link ScriptedResource#getID() ScriptedResource IDs} to files, by resource-type
	 */
	private final Map<Class<?>, Map<String, FileHandle>> scriptedResourceIDs = new LinkedHashMap<>()
	
	private final Map<Class<?>, Map<Class<? extends Throwable>, Set<BiConsumer<AssetDescriptor<?>, Throwable>>>> exceptionHandlers = new LinkedHashMap<>()
	private final LinkedList<Runnable> onLoadActions = new LinkedList<>()
	
	private final List<LoadFailureBean> loadFailures = new LinkedList<>()
	
	private int iterationsWithoutResourceScheduled = 0
	
	/**
	 * When a resource-loading exception is caught, should it be re-thrown if there
	 * are no exception-handlers registered to handle it?
	 */
	boolean throwUnhandledExceptions = true
	
	public GameAssetService() {
		
		super(FILE_HANDLE_RESOLVER)
	}
	
	
	
	@Override
	public synchronized <T, P extends AssetLoaderParameters<T>> void setLoader(Class<T> type, String suffix,
			AssetLoader<T, P> loader) {
		
		if (ScriptedResourceLoader.isAssignableFrom(loader.getClass())) {
			
			scriptResourceLoaders[(Class<ScriptedResource>) type] = (ScriptedResourceLoader) loader
			
			//
			// If a ScriptedResource fails, we should remove it from the list of pending ScriptedResources!
			addFailureHandler(type, Throwable, { asset, ex ->
				
				synchronized(pendingResourceLoads) {
					failedPendingResourceLoads.computeIfAbsent(asset.type, { _ -> new LinkedHashSet<>() }) << asset.fileName
				}
			})
		}
		
		super.setLoader(type, suffix, loader)
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
		
		exceptionHandlers.computeIfAbsent(assetType, { a ->
			new LinkedHashMap<>()
		}).computeIfAbsent(exceptionType, { _ -> new LinkedHashSet<>() }) << exceptionHandler
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
			
			synchronized(pendingResourceLoads) {
				pendingResourceLoads.computeIfAbsent(type, { t ->
					new LinkedHashSet<>()
				}).add fileName
				pendingResourceLoadsCount.incrementAndGet()
			}
		}
		else
			super.load(fileName, type)
	}
	
	/**
	 * Add an action that will be executed once all queued assets are fully loaded.
	 * 
	 * @param action
	 */
	public void addOnLoadAction(Runnable action) {
		
		if (action != null)
			onLoadActions << action
	}
	
	public float getLoadingProgress() {
		
		super.getProgress()
	}
	
	/**
	 * Get all successfully-loaded {@link ScriptedResource}s of the given type.
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
	public <T extends ScriptedResource> Collection<T> getAllByType(Class<T> type) {
		
		final List<T> result = new LinkedList<>()
		for (FileHandle resourceFile : scriptedResourceIDs.computeIfAbsent(type, { _ -> new LinkedHashMap<>() }).values())
			if (isLoaded(resourceFile.path(), type))
				result.add get(resourceFile.path(), type)
		
		result
	}
	
	/**
	 * Get the given {@link ScriptedResource} by ID.
	 * 
	 * @param <T>
	 * @param id
	 * @param type
	 * @return {@code null} if the given ID is unknown, or if the given
	 *         ScriptedResource is not loaded
	 */
	public <T extends ScriptedResource> T getByID(String id, Class<T> type) {
		
		final FileHandle scriptedResourceFile = getFileByID(id, type)
		if (scriptedResourceFile == null)
			return null
		
		if (!isLoaded(scriptedResourceFile.path(), type))
			return null
		
		get(scriptedResourceFile.path(), type)
	}
	
	/**
	 * Unload the given asset.
	 * <p>
	 * If {@code name} corresponds to the ID of a loaded {@link ScriptedResource},
	 * that resource is unloaded.
	 * </p>
	 * <p>
	 * Otherwise, {@code name} is assumed to be a file-name and execution is delegated
	 * to the default {@link AssetManager#unload(String) unload()} behavior.
	 * </p>
	 * @throws IllegalArgumentException if {@code name} corresponds to multiple resource-IDs;
	 * 		if so, you must disambiguate yourself by calling {@link #unload(String, Class)}
	 */
	@Override
	public synchronized void unload(String name) {
		
		final resourceEntries = scriptedResourceIDs.findAll { type, resources -> resources.containsKey(name) }
		
		if(resourceEntries.size() > 1)
			throw new IllegalArgumentException("Cannot unload resource by name -- multiple resources match the given name \"{0}\". You must use unload(name, type) instead!", name)
		
		if(!resourceEntries.isEmpty()) {
			
			for(def resourceEntry : resourceEntries)
				unload name, resourceEntry.key
			
		} else
			super.unload name
	}
	
	/**
	 * Unload the given asset.
	 * 
	 * @param name
	 * @param type
	 */
	public synchronized void unload(String name, Class<?> type) {
		
		if(ScriptedResource.isAssignableFrom(type)) {
			
			final file = scriptedResourceIDs.computeIfAbsent(type, {_ -> new LinkedHashSet<>()}).remove name
			if(file) {
				((ScriptedResourceLoader) getLoader(type)).finishUnloading file
				super.unload file.path()
			}
			
		} else
			super.unload name
	}
	
	/**
	 * Reload the given {@link ScriptedResource}.
	 * <p>
	 * <strong>Note</strong> that this does not wait for the resource to finish loading;
	 * this only schedules the load. You must call {@link #update()} or {@link finishLoading()}
	 * or something to ensure that the load completes.
	 * </p>
	 * 
	 * @param resourceID
	 * @param type
	 */
	public synchronized <T extends ScriptedResource> void reload(String resourceID, Class<T> type) {
		
		if(!scriptedResourceIDs.computeIfAbsent(type, {_ -> new LinkedHashSet<>()}).containsKey(resourceID))
			return
		
		final resourceFile = scriptedResourceIDs[type][resourceID]
		if(!resourceFile)
			return
		
		unload resourceID, type
		load resourceFile.path(), type
	}
	
	/**
	 * Get the {@link FileHandle} associated with the given
	 * {@link ScriptedResource}.
	 * 
	 * @param id
	 * @param type
	 * @return {@code null} if the given ID is unknown
	 */
	public FileHandle getFileByID(String id, Class<? extends ScriptedResource> type) {
		
		scriptedResourceIDs.get(type).get(id)
	}
	
	@Override
	public synchronized boolean update() {
		
		updateResources(super.update())
	}
	
	@Override
	public boolean update(int millis) {
		
		updateResources(super.update(millis))
	}
	
	private boolean updateResources(boolean isAssetsLoaded) {
		
		final boolean allResourcesLoaded = pendingResourceLoadsCount.get() == 0
		
		if (isAssetsLoaded) {
			final boolean atLeastOneScheduled = updatePendingResourceLoads()
			
			if (!atLeastOneScheduled)
				iterationsWithoutResourceScheduled++
			else
				iterationsWithoutResourceScheduled = 0
		}
		
		final boolean isComplete = isAssetsLoaded && allResourcesLoaded
		if (isComplete)
			doOnLoadActions()
		else if (isAssetsLoaded && iterationsWithoutResourceScheduled > 100) {
			//
			// Fail all pending resources.
			synchronized(pendingResourceLoads) {
				while(!pendingResourceLoads.isEmpty()) {
					
					final pendingType = pendingResourceLoads.keySet().first()
					final pendingFileNames = pendingResourceLoads[pendingType]
					
					while(!pendingFileNames.isEmpty()) {
						final pendingFileName = pendingFileNames.first()
						taskFailed(new AssetDescriptor<>(pendingFileName, pendingType), new RuntimeException(
								"Cannot load resource -- possible circular dependency with another resource?"))
						pendingFileNames.remove pendingFileName
					}
					
					pendingResourceLoads.remove pendingType
				}
				
				pendingResourceLoads.clear()
				pendingResourceLoadsCount.set(0)
			}
			
			return true
		}
		
		return isComplete
	}
	
	private boolean updatePendingResourceLoads() {
		
		boolean atLeastOneResourceScheduled = false
		
		synchronized(pendingResourceLoads) {
			for (Entry<Class<?>, Set<String>> pendingResourceLoad : pendingResourceLoads.entrySet()) {
				final Class<?> pendingType = pendingResourceLoad.key
				final Set<String> fileNames = pendingResourceLoad.value
				
				final Iterator<String> fileNameIterator = fileNames.iterator()
				while (fileNameIterator.hasNext()) {
					
					final String fileName = fileNameIterator.next()
					final ScriptedResourceLoader<?, ?> loader = scriptResourceLoaders.get(pendingType)
					final FileHandle fileHandle = loader.resolve(fileName)
					
					//
					// If we've flagged this pending-resource as failed,
					// then we should remove it from the pending-list.
					if(failedPendingResourceLoads[pendingType]?.contains(fileName)) {
						fileNameIterator.remove()
						pendingResourceLoadsCount.decrementAndGet()
						failedPendingResourceLoads[pendingType].remove fileName
						continue
					}
					
					//
					// For the given pending resource, see if it has any resource-dependencies that
					// aren't loaded.
					//
					try {
						
						boolean anyNotLoaded = false
						for (Entry<Class<?>, Set<String>> scriptedDependencies : loader.getScriptedDependencies(fileName)
								.entrySet()) {
							
							final Class<?> dependencyType = scriptedDependencies.key
							for (String dependencyID : scriptedDependencies.value) {
								final FileHandle dependencyFile = scriptedResourceIDs
										.computeIfAbsent(dependencyType, {t -> new LinkedHashMap<>()}).get(dependencyID)
								if (dependencyFile == null || !super.isLoaded(dependencyFile.path(), dependencyType)) {
									anyNotLoaded = true
									break
								}
							}
							
							if (anyNotLoaded)
								break
						}
						
						//
						// If any aren't loaded, then this pending resource can't become non-pending.
						if (anyNotLoaded)
							continue
						
						//
						// Make sure we can reference this resource by its ID.
						final String resourceID = loader.getResourceID(fileHandle)
						scriptedResourceIDs.computeIfAbsent(pendingType, {t -> new LinkedHashMap<>()}).put(resourceID,
						fileHandle)
						
						//
						// Schedule this resource for loading.
						fileNameIterator.remove()
						pendingResourceLoadsCount.decrementAndGet()
						super.load(fileName, pendingType)
						atLeastOneResourceScheduled = true
						
					} catch(Throwable t) {
						handleException new AssetDescriptor(fileHandle, pendingType), t
					}
					
				}
			}
		}
		
		return atLeastOneResourceScheduled
	}
	
	@Override
	public synchronized boolean isFinished() {
		
		final boolean isAssetsLoaded = super.isFinished()
		final boolean noPendingResources = pendingResourceLoadsCount.get() == 0
		
		return (isAssetsLoaded && noPendingResources)
	}
	
	@Override
	public void finishLoading() {
		
		super.finishLoading()
		doOnLoadActions()
	}
	
	public <T> T finishLoading(String assetPath) {
		
		return super.finishLoadingAsset(assetPath)
	}
	
	private void doOnLoadActions() {
		
		while (!onLoadActions.isEmpty())
			onLoadActions.pop().run()
	}
	
	@Override
	protected void taskFailed( AssetDescriptor assetDesc, RuntimeException ex) {
		
		handleException assetDesc, ex
	}
	
	/**
	 * Handle any exception encountered during resource-loading.
	 * @param asset the asset that threw the exception, or {@code null} if unknown/none
	 * @param t
	 */
	private void handleException(AssetDescriptor assetDesc, Throwable ex) {
		try {
			LOG.error "Caught an exception while loading assets!"
			
			if(assetDesc == null)
				LOG.error "Captured exception: {0}", ex.getClass().simpleName
			else
				LOG.error "Captured exception: {0} while loading {1}", ex.getClass().simpleName,
						assetDesc.fileName
			
			LOG.debug("Drilling down to capture root exception ...")
			Throwable t = ex
			while (t.getCause() != null) {
				LOG.debug("Drilling down past {0} ...", t.getClass().getSimpleName())
				t = t.getCause()
			}
			LOG.error(t, "Root exception is {0}: {1}", t.getClass().getSimpleName(), t.getMessage())
			
			if(assetDesc == null) {
				loadFailures.add(LoadFailureBean.forNoResource(ex))
				
				if (isThrowUnhandledExceptions())
					throw ex
			}
			else {
				loadFailures.add(new LoadFailureBean(assetDesc.type, assetDesc.file?.path() ?: assetDesc.fileName, t))
				
				//
				// Execute all exception-handlers for which:
				//  - asset-type matches or is a superclass of the failed-asset
				//  - exception-type matches or is a superclass of the thrown exception
				//
				final Set<BiConsumer<AssetDescriptor<?>, Throwable>> validExceptionHandlers = []
				exceptionHandlers.each { assetType, exceptionHandlers ->
					if(assetType.isAssignableFrom(assetDesc.type))
						exceptionHandlers.each { exceptionType, handlers ->
							if(exceptionType.isAssignableFrom(t.class))
								validExceptionHandlers.addAll handlers
						}
				}
				
				validExceptionHandlers.each { it.accept assetDesc, t }
				
				//
				// Of course -- if we found no exception-handlers, then maybe we
				// have to simply re-throw this exception.
				if(validExceptionHandlers.isEmpty() && throwUnhandledExceptions)
					throw ex
			}
			
		} catch (Throwable t) {
			throw new GdxRuntimeException("Uh oh -- unhandled resource-loading exception!", t)
		}
	}
	
	private void handleForAssetType(AssetDescriptor assetDesc, Throwable t,
			Map<Class<? extends Throwable>, BiConsumer<AssetDescriptor<?>, Throwable>> exceptionHandlers)
	throws Throwable {
		
		//
		//
		// Call all exception-handlers that accept this exception-type or one if its supertypes
		final validExceptionHandlers = exceptionHandlers.findAll { exceptionType, exceptionHandler -> exceptionType.isAssignableFrom(t.class) }
		
		if(validExceptionHandlers.isEmpty())
			throw t
		else
			validExceptionHandlers.each { exceptionType, exceptionHandler -> exceptionHandler.accept assetDesc, t }
	}
	
	/**
	 * @return the list of those resources that failed to load, along with their
	 *         failure-reasons
	 */
	public Collection<LoadFailureBean> getLoadFailures() {
		
		return Collections.unmodifiableCollection(loadFailures)
	}
	
	public static class LoadFailureBean {
		
		public static LoadFailureBean forNoResource(Throwable t) {
			return new LoadFailureBean(null, null, t)
		}
		
		private final Class<?> assetType
		private final String file
		private final Throwable exception
		
		public LoadFailureBean(Class<?> assetType, String file, Throwable exception) {
			
			this.assetType = assetType
			this.file = file
			this.exception = exception
		}
		
		public Class<?> getAssetType() {
			
			return assetType
		}
		
		public String getFile() {
			
			return file
		}
		
		public Throwable getException() {
			
			return exception
		}
	}
}
