package org.snowjak.city.configuration;

import java.util.Collection;
import java.util.LinkedList;

import org.snowjak.city.configuration.processors.InjectAll;
import org.snowjak.city.console.Console;
import org.snowjak.city.console.loggers.ConsoleLoggerFactory;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.map.generator.MapGeneratorLoader;
import org.snowjak.city.map.tiles.TileSet;
import org.snowjak.city.module.Module;
import org.snowjak.city.resources.ScriptedResource;
import org.snowjak.city.resources.ScriptedResourceLoader;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.LoggerService;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.kiwi.log.Logger;

/**
 * Thanks to the Component annotation, this class will be automatically found
 * and processed.
 *
 * This is a utility class that configures application settings.
 */
@Component
public class Configuration {
	
	private static final Logger LOG = LoggerService.forClass(Configuration.class);
	
	/**
	 * The Gdx-managed Preferences file is named so.
	 */
	public static final String PREFERENCES_NAME = "jCity";
	
	public static final String SKIN_NAME = "minty-fresh-ui";
	
	/**
	 * Directory holding module-definition scripts
	 */
	public static final String EXTERNAL_ROOT_MODULES = "data/modules/";
	
	/**
	 * Directory holding tileset-definition scripts
	 */
	public static final String EXTERNAL_ROOT_TILESETS = "data/tilesets/";
	
	@InjectAll(ScriptedResourceLoader.class)
	private Collection<ScriptedResourceLoader<ScriptedResource, ?>> scriptedResourceLoaders;
	
	@Initiate(priority = InitPriority.HIGHEST_PRIORITY)
	public void redirectLoggerToConsole(final Console console) {
		
		LOG.info("Redirecting logging to in-game console ...");
		ConsoleLoggerFactory.get().setConsole(console);
	}
	
	@Initiate(priority = InitPriority.HIGHEST_PRIORITY)
	public void configureAssetLoaders(final GameAssetService assetService, FileHandleResolver resolver) {
		
		final MapGeneratorLoader mapGeneratorLoader = new MapGeneratorLoader(resolver);
		assetService.setLoader(MapGenerator.class, mapGeneratorLoader);
		
		scriptedResourceLoaders.forEach(l -> assetService.setLoader(l.getResourceType(), l));
		
		initiateScriptScanning(assetService, resolver);
	}
	
	private void initiateScriptScanning(final GameAssetService assetService, FileHandleResolver resolver) {
		
		LOG.info("Scanning for resource-scripts ...");
		
		LOG.info("Scanning for resource-scripts: tile-sets ...");
		scanForFiles(resolver.resolve(EXTERNAL_ROOT_TILESETS), ".tileset.groovy", true).forEach(f -> {
			LOG.info("Queueing tile-set for load: [{0}] ...", f.path());
			assetService.load(f.path(), TileSet.class);
		});
		
		LOG.info("Scanning for resource-scripts: modules ...");
		scanForFiles(resolver.resolve(EXTERNAL_ROOT_MODULES), ".module.groovy", true).forEach(f -> {
			LOG.info("Queueing module for load: [{0}] ...", f.path());
			assetService.load(f.path(), Module.class);
		});
	}
	
	private Collection<FileHandle> scanForFiles(FileHandle directory, String desiredExtension,
			boolean includeSubdirectories) {
		
		final LinkedList<FileHandle> result = new LinkedList<>();
		
		for (FileHandle child : directory.list())
			if (child.isDirectory()) {
				if (includeSubdirectories)
					result.addAll(scanForFiles(child, desiredExtension, includeSubdirectories));
			} else if (child.name().endsWith(desiredExtension))
				result.add(child);
			
		return result;
	}
}