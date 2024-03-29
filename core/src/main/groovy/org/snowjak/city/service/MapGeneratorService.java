/**
 * 
 */
package org.snowjak.city.service;

import org.snowjak.city.CityGame;
import org.snowjak.city.configuration.InitPriority;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.map.generator.support.MapGeneratorDsl;

import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;

/**
 * Provides access by name to {@link MapGeneratorDsl map-generator scripts}.
 * 
 * @author snowjak88
 *
 */
@Component
public class MapGeneratorService extends AbstractResourceService<MapGenerator, MapGenerator> {
	
	public MapGeneratorService(GameAssetService assetService) {
		
		super(MapGenerator.class, assetService,
				GameAssetService.FILE_HANDLE_RESOLVER.resolve(CityGame.EXTERNAL_ROOT_MAP_GENERATORS), true,
				".mapgen.groovy");
	}
	
	@Initiate(priority = InitPriority.VERY_HIGH_PRIORITY)
	public void init() {
		
		initInternal();
	}
}
