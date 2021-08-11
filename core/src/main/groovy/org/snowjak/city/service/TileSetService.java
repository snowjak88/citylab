/**
 * 
 */
package org.snowjak.city.service;

import org.snowjak.city.CityGame;
import org.snowjak.city.map.tiles.TileSet;

import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.Provider;
import com.github.czyzby.autumn.mvc.component.asset.AssetService;
import com.github.czyzby.autumn.mvc.config.AutumnActionPriority;
import com.github.czyzby.autumn.provider.DependencyProvider;

/**
 * Provides Tileset-related application-level services.
 * 
 * @author snowjak88
 *
 */
@Component
public class TileSetService extends AbstractScriptService<TileSet, TileSet> {
	
	@Provider
	public static class TileSetServiceProvider implements DependencyProvider<TileSetService> {
		
		@Inject
		private AssetService assetService;
		
		@Override
		public Class<TileSetService> getDependencyType() {
			
			return TileSetService.class;
		}
		
		@Override
		public TileSetService provide() {
			
			return new TileSetService(assetService);
		}
	}
	
	public TileSetService(AssetService assetService) {
		
		super(TileSet.class, (t) -> t, assetService, CityGame.RESOLVER.resolve(CityGame.EXTERNAL_ROOT_TILESETS), true,
				".tileset.groovy");
	}
	
	@Initiate(priority = AutumnActionPriority.LOW_PRIORITY)
	@Override
	public void init() {
		
		initInternal();
	}
}
