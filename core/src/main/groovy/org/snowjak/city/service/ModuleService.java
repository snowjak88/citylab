/**
 * 
 */
package org.snowjak.city.service;

import org.snowjak.city.CityGame;
import org.snowjak.city.module.Module;

import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.mvc.config.AutumnActionPriority;

/**
 * @author snowjak88
 *
 */
@Component
public class ModuleService extends AbstractResourceService<Module, Module> {
	
	/**
	 * @param toLoadType
	 * @param assetService
	 * @param baseDirectory
	 * @param includeSubdirectories
	 * @param extension
	 */
	public ModuleService(GameAssetService assetService) {
		
		super(Module.class, assetService, CityGame.RESOLVER.resolve(CityGame.EXTERNAL_ROOT_MODULES), true,
				".module.groovy");
	}
	
	@Initiate(priority = AutumnActionPriority.VERY_LOW_PRIORITY)
	@Override
	public void init() {
		
		initInternal();
	}
}
