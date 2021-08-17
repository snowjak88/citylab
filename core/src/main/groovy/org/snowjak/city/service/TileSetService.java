/**
 * 
 */
package org.snowjak.city.service;

import org.snowjak.city.CityGame;
import org.snowjak.city.configuration.InitPriority;
import org.snowjak.city.map.tiles.TileSet;

import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;

/**
 * Provides Tileset-related application-level services.
 * 
 * @author snowjak88
 *
 */
@Component
public class TileSetService extends AbstractResourceService<TileSet, TileSet> {
	
	public TileSetService(GameAssetService assetService) {
		
		super(TileSet.class, assetService, CityGame.RESOLVER.resolve(CityGame.EXTERNAL_ROOT_TILESETS), true,
				".tileset.groovy");
	}
	
	@Initiate(priority = InitPriority.VERY_HIGH_PRIORITY)
	@Override
	public void init() {
		
		initInternal();
	}
}
