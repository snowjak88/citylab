/**
 * 
 */
package org.snowjak.city.module;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.tiles.Tile;
import org.snowjak.city.map.tiles.TileCorner;
import org.snowjak.city.map.tiles.TileEdge;
import org.snowjak.city.map.tiles.TileSet;
import org.snowjak.city.module.ModuleResourceLoader.ModuleResourceLoaderParameters;
import org.snowjak.city.resources.ScriptedResourceLoader;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.PreferencesService;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.github.czyzby.autumn.annotation.Component;
import com.google.common.util.concurrent.ListenableFuture;

import groovy.util.DelegatingScript;

/**
 * @author snowjak88
 *
 */
@Component
public class ModuleResourceLoader extends ScriptedResourceLoader<Module, ModuleResourceLoaderParameters> {
	
	private final GameService gameService;
	private final PreferencesService preferencesService;
	private final I18NService i18nService;
	
	public ModuleResourceLoader(GameService gameService, PreferencesService preferencesService,
			GameAssetService assetService, I18NService i18nService) {
		
		super(assetService);
		
		this.gameService = gameService;
		this.preferencesService = preferencesService;
		this.i18nService = i18nService;
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
		customizer.addImport("Buttons", Input.Buttons.class.getName());
		customizer.addImport("Poolable", Poolable.class.getName());
		customizer.addImports(
				// jCity types
				CityMap.class.getName(), Tile.class.getName(), TileSet.class.getName(), TileCorner.class.getName(),
				TileEdge.class.getName(),
				// Misc. LibGDX types
				AssetDescriptor.class.getName(), Color.class.getName(),
				// Ashley ECS types
				ComponentMapper.class.getName(), Entity.class.getName(),
				// background-task types
				ListenableFuture.class.getName());
		
		config.addCompilationCustomizers(customizer);
		config.setScriptBaseClass(DelegatingScript.class.getName());
		return config;
	}
	
	@Override
	protected Module newInstance() {
		
		return new Module(gameService, preferencesService, i18nService);
	}
	
	@Override
	protected void afterLoad(Module resource, GameAssetService assetService, boolean isDependencyMode) {
		
		if (isDependencyMode)
			resource.getI18n().getBundles().forEach(b -> assetService.load(b.path(), I18NBundle.class));
	}
	
	@Override
	public Class<Module> getResourceType() {
		
		return Module.class;
	}
	
	public static class ModuleResourceLoaderParameters extends AssetLoaderParameters<Module> {
	}
}
