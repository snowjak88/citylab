/**
 * 
 */
package org.snowjak.city.service;

import org.snowjak.city.CityGame;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.map.generator.support.MapGeneratorDsl;

import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.mvc.config.AutumnActionPriority;

/**
 * Provides access by name to {@link MapGeneratorDsl map-generator scripts}.
 * 
 * @author snowjak88
 *
 */
@Component
public class MapGeneratorService extends AbstractResourceService<MapGenerator, MapGenerator> {
	
	public MapGeneratorService(GameAssetService assetService) {
		
		super(MapGenerator.class, assetService, CityGame.RESOLVER.resolve(CityGame.EXTERNAL_ROOT_MAP_GENERATORS), true,
				".mapgen.groovy");
	}
	
	@Initiate(priority = AutumnActionPriority.LOW_PRIORITY)
	public void init() {
		
		initInternal();
	}
}
