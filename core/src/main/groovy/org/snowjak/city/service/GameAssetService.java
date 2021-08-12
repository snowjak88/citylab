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

/**
 * Upgrades the stock {@link AssetManager} to support customized load-failure
 * handling.
 * 
 * @author snowjak88
 *
 */
@Component
public class GameAssetService extends AssetManager {
	
	private final Map<Class<?>, Map<Class<? extends RuntimeException>, BiConsumer<AssetDescriptor<?>, RuntimeException>>> exceptionHandlers = new LinkedHashMap<>();
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
	public <T, E extends RuntimeException> void addFailureHandler(Class<T> assetType, Class<E> exceptionType,
			BiConsumer<AssetDescriptor<?>, RuntimeException> exceptionHandler) {
		
		exceptionHandlers.computeIfAbsent(assetType, (a) -> new LinkedHashMap<>()).put(exceptionType, exceptionHandler);
	}
	
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
		
		final RuntimeException exception;
		Throwable t = ex;
		while (t.getCause() != null && t instanceof RuntimeException)
			t = t.getCause();
		
		if (t instanceof GdxRuntimeException)
			t = ex;
		exception = (RuntimeException) t;
		
		loadFailures.add(new LoadFailureBean(assetDesc.type, assetDesc.file, exception));
		
		if (exceptionHandlers.containsKey(assetDesc.type))
			//
			// We have an exception-handler that matches this asset-type
			handleForAssetType(assetDesc, exception, exceptionHandlers.get(assetDesc.type));
		
		else {
			
			final Map<Class<? extends RuntimeException>, BiConsumer<AssetDescriptor<?>, RuntimeException>> handlers = exceptionHandlers
					.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(assetDesc.type)).findFirst()
					.map(Entry::getValue).orElse(null);
			
			if (handlers != null)
				//
				// We have an exception handler for a superclass of this asset-type
				handleForAssetType(assetDesc, exception, handlers);
			else
				//
				// No valid exception-handlers. Just throw it.
				throw ex;
		}
	}
	
	private void handleForAssetType(@SuppressWarnings("rawtypes") AssetDescriptor assetDesc, RuntimeException ex,
			Map<Class<? extends RuntimeException>, BiConsumer<AssetDescriptor<?>, RuntimeException>> exceptionHandlers) {
		
		//
		// Look for exception-handlers that match the exception-class
		if (exceptionHandlers.containsKey(ex.getClass()))
			exceptionHandlers.get(ex.getClass()).accept(assetDesc, ex);
			
		//
		// Look for the first exception-handler that handles a superclass of this
		// exception-class
		final BiConsumer<AssetDescriptor<?>, RuntimeException> exceptionHandler = exceptionHandlers.entrySet().stream()
				.filter(e -> e.getKey().isAssignableFrom(ex.getClass())).findFirst().map(Entry::getValue).orElse(null);
		
		if (exceptionHandler != null)
			exceptionHandler.accept(assetDesc, ex);
		else
			throw ex;
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
		private final RuntimeException exception;
		
		public LoadFailureBean(Class<?> assetType, FileHandle file, RuntimeException exception) {
			
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
		
		public RuntimeException getException() {
			
			return exception;
		}
	}
}
