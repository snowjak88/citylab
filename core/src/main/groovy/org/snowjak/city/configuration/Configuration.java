package org.snowjak.city.configuration;

import org.snowjak.city.CityGame;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.map.generator.MapGeneratorLoader;
import org.snowjak.city.map.tiles.TileSet;
import org.snowjak.city.map.tiles.TileSetLoader;
import org.snowjak.city.module.Module;
import org.snowjak.city.module.ModuleLoader;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.SkinService;
import org.snowjak.city.service.TileSetService;

import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;
import com.kotcrab.vis.ui.VisUI;

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
	
	/**
	 * Thanks to the Initiate annotation, this method will be automatically invoked
	 * during context building. All method's parameters will be injected with values
	 * from the context.
	 *
	 * @param scaleService
	 *            contains current GUI scale.
	 * @param skinService
	 *            contains GUI skin.
	 */
	@Initiate(priority = InitPriority.HIGHEST_PRIORITY)
	public void initiateConfiguration(SkinService skinService, final GameAssetService assetService,
			final TileSetService tilesetService) {
		
		VisUI.load();
		
		skinService.addSkin("default", VisUI.getSkin());
		
		addCustomAssetLoaders(assetService, tilesetService);
	}
	
	private void addCustomAssetLoaders(GameAssetService assetService, TileSetService tilesetService) {
		
		final TileSetLoader tileSetLoader = new TileSetLoader(CityGame.RESOLVER);
		assetService.setLoader(TileSet.class, tileSetLoader);
		
		final MapGeneratorLoader mapGeneratorLoader = new MapGeneratorLoader(CityGame.RESOLVER);
		assetService.setLoader(MapGenerator.class, mapGeneratorLoader);
		
		final ModuleLoader moduleLoader = new ModuleLoader(CityGame.RESOLVER, tilesetService);
		assetService.setLoader(Module.class, moduleLoader);
	}
}