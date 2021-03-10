/**
 * 
 */
package org.snowjak.city.map.generator;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.snowjak.city.map.generator.MapGeneratorLoader.MapGeneratorLoaderParameters;
import org.snowjak.city.map.generator.support.MapGeneratorScript;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;
import com.sudoplay.joise.module.ModuleBasisFunction.BasisType;
import com.sudoplay.joise.module.ModuleBasisFunction.InterpolationType;
import com.sudoplay.joise.module.ModuleFractal.FractalType;
import com.sudoplay.joise.module.ModuleFunctionGradient.FunctionGradientAxis;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * @author snowjak88
 *
 */
public class MapGeneratorLoader
		extends AsynchronousAssetLoader<MapGeneratorScript, MapGeneratorLoaderParameters> {
	
	private static final Logger LOG = LoggerService.forClass(MapGeneratorLoader.class);
	
	final CompilerConfiguration config;
	
	private MapGeneratorScript script;
	
	public MapGeneratorLoader(FileHandleResolver fileHandleResolver) {
		
		super(fileHandleResolver);
		
		final ImportCustomizer customImports = new ImportCustomizer();
		customImports.addStaticStars(BasisType.class.getName(), InterpolationType.class.getName(),
				FractalType.class.getName(), FunctionGradientAxis.class.getName());
		
		config = new CompilerConfiguration();
		config.setScriptBaseClass(MapGeneratorScript.class.getName());
		config.addCompilationCustomizers(customImports);
		
	}
	
	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file,
			MapGeneratorLoaderParameters parameter) {
		
		if (file == null)
			throw new NullPointerException();
		
		if (!file.exists())
			throw new RuntimeException(new FileNotFoundException());
		
		try {
			final GroovyShell shell = new GroovyShell(this.getClass().getClassLoader(), new Binding(), config);
			script = (MapGeneratorScript) shell.parse(file.file());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		try {
			
			script.run();
			
		} catch (Exception e) {
			LOG.error(e, "Cannot load map-generation script \"{0}\" -- unexpected exception.", file.path());
			throw new RuntimeException(
					"Cannot load map-generation script \"" + file.path() + "\" -- unexpected exception.", e);
		}
		
		if (script.getBinding().getVariable("altitude") == null) {
			LOG.error("Cannot load map-generation script \"{0}\" -- does not set [altitude].", file.path());
			throw new RuntimeException(
					"Map-generation script \"" + file.path() + "\" is incomplete: does not set \"altitude\".");
		}
		
		if (script.getBinding().getVariable("material") == null) {
			LOG.error("Cannot load map-generation script \"{0}\" -- does not set [material].", file.path());
			throw new RuntimeException(
					"Map-generation script \"" + file.path() + "\" is incomplete: does not set \"material\".");
		}
		
	}
	
	@Override
	public MapGeneratorScript loadSync(AssetManager manager, String fileName, FileHandle file,
			MapGeneratorLoaderParameters parameter) {
		
		return script;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file,
			MapGeneratorLoaderParameters parameter) {
		
		return null;
	}
	
	public static class MapGeneratorLoaderParameters extends AssetLoaderParameters<MapGeneratorScript> {
		
	}
}
