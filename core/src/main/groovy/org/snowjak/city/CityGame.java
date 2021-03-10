package org.snowjak.city;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.groovy.util.Maps;
import org.snowjak.city.configuration.MatchingFileHandleResolver;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * This class serves only as the application scanning root. Any classes in its
 * package (or any of the sub-packages) with proper Autumn MVC annotations will
 * be found, scanned and initiated.
 */
public class CityGame {
	
	/** Default application size. */
	public static final int WIDTH = 800, HEIGHT = 600;
	
	/**
	 * The default tile-set descriptor is located here. It can be loaded using
	 * {@link AssetManager#get(String, Class) AssetManager.get(DEFAULT_TILESET_PATH,
	 * TileSet.class)}
	 */
	public static final String DEFAULT_TILESET_PATH = "data/tilesets/default.groovy";
	
	public static final String EXTERNAL_ROOT_BUNDLES = "data/bundles/";
	public static final String EXTERNAL_ROOT_MAP_GENERATORS = "data/map-generators/";
	
	/**
	 * Application-specific {@link FileHandleResolver}, configured to handle
	 * internal- and external-resources equally well.
	 */
	public static final FileHandleResolver RESOLVER = new MatchingFileHandleResolver(
			Maps.of("^/?data/.*", new LocalFileHandleResolver()), new InternalFileHandleResolver());
	
	/**
	 * {@link Executor} for scheduled threads.
	 */
	public static final ListeningScheduledExecutorService SCHEDULED_EXECUTOR = MoreExecutors
			.listeningDecorator(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() - 1));
}