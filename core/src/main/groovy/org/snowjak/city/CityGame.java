package org.snowjak.city;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.badlogic.gdx.assets.AssetManager;

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
	public static final String DEFAULT_TILESET_PATH = "images/tilesets/terrain/default/tileset.json";
	
	/**
	 * {@link Executor} for scheduled threads.
	 */
	public static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors
			.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() - 1);
}