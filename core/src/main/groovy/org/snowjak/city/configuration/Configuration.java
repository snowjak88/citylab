package org.snowjak.city.configuration;

import org.snowjak.city.CityGame;
import org.snowjak.city.console.Console;
import org.snowjak.city.console.loggers.ConsoleLoggerFactory;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.map.generator.MapGeneratorLoader;
import org.snowjak.city.map.tiles.TileSet;
import org.snowjak.city.map.tiles.TileSetResourceLoader;
import org.snowjak.city.module.Module;
import org.snowjak.city.module.ModuleResourceLoader;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.LoggerService;
import org.snowjak.city.service.TileSetService;

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
	 * Thanks to the Initiate annotation, this method will be automatically invoked
	 * during context building. All method's parameters will be injected with values
	 * from the context.
	 *
	 * @param scaleService
	 *            contains current GUI scale.
	 */
	@Initiate(priority = InitPriority.HIGHEST_PRIORITY)
	public void configureAssetLoaders(final GameAssetService assetService, final TileSetService tilesetService) {
		
		final TileSetResourceLoader tileSetLoader = new TileSetResourceLoader(assetService, CityGame.RESOLVER);
		assetService.setLoader(TileSet.class, tileSetLoader);
		
		final MapGeneratorLoader mapGeneratorLoader = new MapGeneratorLoader(CityGame.RESOLVER);
		assetService.setLoader(MapGenerator.class, mapGeneratorLoader);
		
		final ModuleResourceLoader moduleLoader = new ModuleResourceLoader(tilesetService, assetService,
				CityGame.RESOLVER);
		assetService.setLoader(Module.class, moduleLoader);
		assetService.addScriptedResourceLoader(Module.class, moduleLoader);
	}
	
	@Initiate(priority = InitPriority.HIGHEST_PRIORITY)
	public void redirectLoggerToConsole(final Console console) {
		
		LOG.info("Redirecting logging to in-game console ...");
		ConsoleLoggerFactory.get().setConsole(console);
	}
}