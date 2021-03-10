/**
 * 
 */
package org.snowjak.city.map.tiles;

import static org.snowjak.city.util.Util.max;
import static org.snowjak.city.util.Util.min;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.snowjak.city.map.tiles.TileSetLoader.TileSetLoaderParameters;
import org.snowjak.city.map.tiles.support.TileSetDescriptorSpec;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.DelegatingScript;

/**
 * Custom {@link AssetLoader} to load a tile-set definition and explode it into
 * a {@link TiledMapTileSet}.
 * 
 * @author snowjak88
 *
 */
public class TileSetLoader extends AsynchronousAssetLoader<TileSet, TileSetLoaderParameters> {
	
	/**
	 * Property stored in {@link TiledMapTileSet#getProperties()}: title assigned to
	 * the tile-set.
	 */
	public static final String PROPERTIES_TILESET_TITLE = "tileset-title",
			/**
			 * Property stored in {@link TiledMapTileSet#getProperties()}:
			 * tile-set-descriptor file-name
			 */
			PROPERTIES_TILESET_FILENAME = "tileset-file",
			/**
			 * Property stored in {@link TiledMapTile#getProperties()}: title assigned to
			 * the tile.
			 */
			PROPERTIES_TILE_TITLE = "tile-title";
	
	private static final Logger LOG = LoggerService.forClass(TileSetLoader.class);
	
	final CompilerConfiguration config;
	
	private TileSetDescriptor tileSetDescriptor;
	
	private TileSet tileSet;
	
	/**
	 * Construct a new {@link TileSetLoader}.
	 * 
	 * @param resolver
	 */
	public TileSetLoader(FileHandleResolver resolver) {
		
		super(resolver);
		
		final ImportCustomizer customImports = new ImportCustomizer();
		customImports.addStaticStars();
		
		config = new CompilerConfiguration();
		config.setScriptBaseClass(DelegatingScript.class.getName());
		config.addCompilationCustomizers(customImports);
	}
	
	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, TileSetLoaderParameters parameter) {
		
	}
	
	@Override
	public TileSet loadSync(AssetManager manager, String fileName, FileHandle file, TileSetLoaderParameters parameter) {
		
		//
		// Load the specified image for each tile-descriptor,
		// create the resulting TiledMapTile instance,
		// and insert it into the TileSet.
		//
		for (TileDescriptor td : tileSet.getTileSetDescriptor().getAllTiles()) {
			
			final FileHandle imageFile = file.parent().child(td.getFilename());
			if (!imageFile.exists())
				throw new RuntimeException(new FileNotFoundException("Cannot load tile-set \"" + file.path()
						+ "\" -- referenced image-file \"" + imageFile.path() + "\" does not exist."));
			
			manager.load(imageFile.path(), Texture.class);
			manager.finishLoadingAsset(imageFile.path());
			final Texture imageTexture = manager.get(imageFile.path(), Texture.class);
			
			//
			// Calculate safe texture-region dimensions, clamping the specified to the
			// actual texture-dimensions.
			final int startX = min(max(0, td.getX() + td.getPadding()), imageTexture.getWidth() - 1);
			final int startY = min(max(0, td.getY() + td.getPadding()), imageTexture.getHeight() - 1);
			final int width = max(min(td.getWidth(), imageTexture.getWidth() - td.getPadding()), 0);
			final int height = max(min(td.getHeight(), imageTexture.getHeight() - td.getPadding()), 0);
			
			final TextureRegion imageRegion = new TextureRegion(imageTexture, startX, startY, width, height);
			final TiledMapTile tile = new StaticTiledMapTile(imageRegion);
			tileSet.putTile(td.getHashcode(), tile);
			
		}
		
		return tileSet;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, TileSetLoaderParameters parameter) {
		
		final Array<AssetDescriptor> dependencies = new Array<>();
		
		if (file == null)
			throw new NullPointerException();
		
		if (!file.exists())
			throw new RuntimeException(new FileNotFoundException());
		
		final TileSetDescriptorSpec spec = new TileSetDescriptorSpec();
		final DelegatingScript script;
		try {
			
			final GroovyShell shell = new GroovyShell(this.getClass().getClassLoader(), new Binding(), config);
			script = (DelegatingScript) shell.parse(file.file());
			script.setDelegate(spec);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		try {
			
			script.run();
			tileSet = new TileSet(file, spec.build());
			
			for (TileDescriptor td : tileSet.getTileSetDescriptor().getAllTiles()) {
				dependencies.add(new AssetDescriptor<>(file.parent().child(td.getFilename()), Texture.class));
			}
			
		} catch (Exception e) {
			LOG.error(e, "Cannot load tile-set \"{0}\" -- unexpected exception.", file.path());
			throw new RuntimeException("Cannot load tile-set \"" + file.path() + "\" -- unexpected exception.", e);
		}
		
		return dependencies;
	}
	
	public static class TileSetLoaderParameters extends AssetLoaderParameters<TileSet> {
		
	}
}
