/**
 * 
 */
package org.snowjak.city.service;

import org.snowjak.city.CityGame;
import org.snowjak.city.module.Module;

import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.Provider;
import com.github.czyzby.autumn.mvc.component.asset.AssetService;
import com.github.czyzby.autumn.mvc.config.AutumnActionPriority;
import com.github.czyzby.autumn.provider.DependencyProvider;

/**
 * @author snowjak88
 *
 */
@Component
public class ModuleService extends AbstractScriptService<Module, Module> {
	
	@Provider
	public static class ModuleServiceProvider implements DependencyProvider<ModuleService> {
		
		@Inject
		private AssetService assetService;
		
		@Inject
		private TileSetService tilesetService;
		
		@Override
		public Class<ModuleService> getDependencyType() {
			
			return ModuleService.class;
		}
		
		@Override
		public ModuleService provide() {
			
			return new ModuleService(assetService, tilesetService);
		}
	}
	
	/**
	 * @param toLoadType
	 * @param assetService
	 * @param baseDirectory
	 * @param includeSubdirectories
	 * @param extension
	 */
	public ModuleService(AssetService assetService, TileSetService tilesetService) {
		
		super(Module.class, (m) -> m, assetService, CityGame.RESOLVER.resolve(CityGame.EXTERNAL_ROOT_MODULES), true,
				".module.groovy");
	}

	@Initiate(priority = AutumnActionPriority.VERY_LOW_PRIORITY)
	@Override
	public void init() {
		
		initInternal();
	}
}
