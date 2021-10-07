/**
 * 
 */
package org.snowjak.city.module;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.snowjak.city.CityGame;
import org.snowjak.city.input.modifiers.ModifierKey;
import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.renderer.MapMode;
import org.snowjak.city.map.tiles.Tile;
import org.snowjak.city.map.tiles.TileCorner;
import org.snowjak.city.map.tiles.TileEdge;
import org.snowjak.city.map.tiles.TileSet;
import org.snowjak.city.module.ModuleResourceLoader.ModuleResourceLoaderParameters;
import org.snowjak.city.module.ui.ModuleWindow;
import org.snowjak.city.resources.ScriptedResourceLoader;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.PreferencesService;
import org.snowjak.city.service.SkinService;

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
	private final SkinService skinService;
	private final I18NService i18nService;
	
	private final Map<String, MapMode> mapModes = new HashMap<>();
	
	public ModuleResourceLoader(GameService gameService, PreferencesService preferencesService,
			GameAssetService assetService, SkinService skinService, I18NService i18nService) {
		
		super(assetService);
		
		this.gameService = gameService;
		this.preferencesService = preferencesService;
		this.skinService = skinService;
		this.i18nService = i18nService;
		
		this.mapModes.putAll(gameService.getState().getMapModes());
		
		this.addSharedClasses(
				resolve(CityGame.EXTERNAL_ROOT_MODULES).child(CityGame.RESOURCE_SHARED_CLASSES_DIRECTORY_NAME));
	}
	
	@Override
	protected CompilerConfiguration getDefaultCompilerConfiguration() {
		
		final CompilerConfiguration config = super.getDefaultCompilerConfiguration();
		
		final ImportCustomizer customizer = getDefaultImportCustomizer();
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
	
	private ImportCustomizer getDefaultImportCustomizer() {
		
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
		customizer.addImport("WindowPin", ModuleWindow.WindowPin.class.getName());
		
		customizer.addStaticStars(ModifierKey.class.getName());
		
		return customizer;
	}
	
	@Override
	protected Module newInstance() {
		
		final Module module = new Module(gameService, preferencesService, skinService, i18nService);
		getAssetService().getAllByType(Module.class)
				.forEach(m -> module.getModules().put(m.getId(), new ModulePublicFace(m)));
		module.getMapModes().putAll(this.mapModes);
		return module;
	}
	
	@Override
	protected void afterLoad(Module resource, GameAssetService assetService, boolean isDependencyMode) {
		
		if (isDependencyMode)
			resource.getI18n().getBundles().forEach(b -> assetService.load(b.path(), I18NBundle.class));
		
		else {
			this.mapModes.putAll(resource.getMapModes());
		}
	}
	
	@Override
	public Class<Module> getResourceType() {
		
		return Module.class;
	}
	
	public static class ModuleResourceLoaderParameters extends AssetLoaderParameters<Module> {
	}
}
