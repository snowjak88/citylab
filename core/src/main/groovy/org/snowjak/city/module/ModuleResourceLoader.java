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
import org.snowjak.city.service.PreferencesService;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.github.czyzby.autumn.annotation.Component;

import groovy.util.DelegatingScript;

/**
 * @author snowjak88
 *
 */
@Component
public class ModuleResourceLoader extends ScriptedResourceLoader<Module, ModuleResourceLoaderParameters> {
	
	private final PreferencesService preferencesService;
	
	public ModuleResourceLoader(PreferencesService preferencesService, GameAssetService assetService,
			FileHandleResolver resolver) {
		
		super(assetService, resolver);
		
		this.preferencesService = preferencesService;
	}
	
	@Override
	protected CompilerConfiguration getDefaultCompilerConfiguration() {
		
		final CompilerConfiguration config = super.getDefaultCompilerConfiguration();
		
		final ImportCustomizer customizer = new ImportCustomizer();
		customizer.addStarImports("org.snowjak.city.ecs.components");
		customizer.addStarImports("com.badlogic.ashley.core");
		customizer.addStarImports("com.badlogic.gdx.audio");
		customizer.addStarImports("com.badlogic.gdx.files");
		customizer.addStarImports("com.badlogic.gdx.graphics");
		customizer.addStarImports("com.badlogic.gdx.math");
		customizer.addStarImports("com.badlogic.gdx.utils");
		customizer.addImports(
				// jCity types
				CityMap.class.getName(), GameData.class.getName(), Tile.class.getName(), TileSet.class.getName(),
				// Misc. LibGDX types
				AssetDescriptor.class.getName());
		
		config.addCompilationCustomizers(customizer);
		config.setScriptBaseClass(DelegatingScript.class.getName());
		return config;
	}
	
	@Override
	protected Module newInstance() {
		
		return new Module(preferencesService);
	}
	
	@Override
	public Class<Module> getResourceType() {
		
		return Module.class;
	}
	
	public static class ModuleResourceLoaderParameters extends AssetLoaderParameters<Module> {
	}
}
