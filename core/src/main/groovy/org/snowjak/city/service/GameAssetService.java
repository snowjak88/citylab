/**
 * 
 */
package org.snowjak.city.service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.snowjak.city.CityGame;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

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
	
	private final Map<Class<?>, Map<Class<? extends Throwable>, BiConsumer<AssetDescriptor<?>, Throwable>>> exceptionHandlers = new LinkedHashMap<>();
	private final List<Runnable> onLoadActions = new LinkedList<>();
	
	private final List<LoadFailureBean> loadFailures = new LinkedList<>();
	
	public GameAssetService() {
		
		super(CityGame.RESOLVER);
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
	 * Add an action that will be executed once all queued assets are fully loaded.
	 * 
	 * @param action
	 */
	public void addOnLoadAction(Runnable action) {
		
		onLoadActions.add(action);
	}
	
	/**
	 * Alias for {@link #getProgress()}.
	 * 
	 * @return
	 */
	public float getLoadingProgress() {
		
		return super.getProgress();
	}
	
	@Override
	public synchronized boolean update() {
		
		final boolean isLoaded = super.update();
		
		if (isLoaded)
			doOnLoadActions();
		
		return isLoaded;
	}
	
	@Override
	public boolean update(int millis) {
		
		final boolean isLoaded = super.update(millis);
		
		if (isLoaded)
			doOnLoadActions();
		
		return isLoaded;
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
		
		for (Runnable action : onLoadActions)
			if (action != null)
				action.run();
		onLoadActions.clear();
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
