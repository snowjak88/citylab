package org.snowjak.city;

import org.apache.groovy.util.Maps;
import org.snowjak.city.configuration.MatchingFileHandleResolver;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;

/**
 * This class serves only as the application scanning root. Any classes in its
 * package (or any of the sub-packages) with proper Autumn MVC annotations will
 * be found, scanned and initiated.
 */
public class CityGame {
	
	/** Default application size. */
	public static final int WIDTH = 800, HEIGHT = 600;
	
	public static final String EXTERNAL_ROOT_BUNDLES = "data/bundles/";
	public static final String EXTERNAL_ROOT_MAP_GENERATORS = "data/map-generators/";
	public static final String EXTERNAL_ROOT_MODULES = "data/modules/";
	public static final String EXTERNAL_ROOT_TILESETS = "data/tilesets/";
	
	/**
	 * Application-specific {@link FileHandleResolver}, configured to handle
	 * internal- and external-resources equally well.
	 */
	public static final FileHandleResolver RESOLVER = new MatchingFileHandleResolver(
			Maps.of("^/?data/.*", new LocalFileHandleResolver()), new InternalFileHandleResolver());
}