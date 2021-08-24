/**
 * 
 */
package org.snowjak.city.module;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.snowjak.city.GameData;
import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.tiles.Tile;
import org.snowjak.city.map.tiles.TileSet;
import org.snowjak.city.module.ModuleResourceLoader.ModuleResourceLoaderParameters;
import org.snowjak.city.resources.ScriptedResourceLoader;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.TileSetService;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;

import groovy.util.DelegatingScript;

/**
 * @author snowjak88
 *
 */
public class ModuleResourceLoader extends ScriptedResourceLoader<Module, ModuleResourceLoaderParameters> {
	
	private final TileSetService tilesetService;
	
	public ModuleResourceLoader(TileSetService tilesetService, GameAssetService assetService,
			FileHandleResolver resolver) {
		
		super(assetService, resolver);
		
		this.tilesetService = tilesetService;
	}
	
	@Override
	protected CompilerConfiguration getDefaultCompilerConfiguration() {
		
		final CompilerConfiguration config = super.getDefaultCompilerConfiguration();
		
		final ImportCustomizer customizer = new ImportCustomizer();
		customizer.addStarImports("org.snowjak.city.ecs.components");
		customizer.addStarImports("com.badlogic.ashley.core");
		customizer.addImports(
				// jCity types
				CityMap.class.getName(), GameData.class.getName(), Tile.class.getName(), TileSet.class.getName());
		
		config.addCompilationCustomizers(customizer);
		config.setScriptBaseClass(DelegatingScript.class.getName());
		return config;
	}
	
	@Override
	protected Module newInstance() {
		
		return new Module(tilesetService);
	}
	
	public static class ModuleResourceLoaderParameters extends AssetLoaderParameters<Module> {
	}
}
