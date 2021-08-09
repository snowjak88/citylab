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
import org.snowjak.city.map.tiles.support.TileSetDsl;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
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
	
	private TileSet tileSet;
	
	/**
	 * Construct a new {@link TileSetLoader}.
	 * 
	 * @param resolver
	 */
	public TileSetLoader(FileHandleResolver resolver) {
		
		super(resolver);
		
		final ImportCustomizer customImports = new ImportCustomizer();
		customImports.addStaticStars(TileCorner.class.getName());
		
		config = new CompilerConfiguration();
		config.setScriptBaseClass(DelegatingScript.class.getName());
		config.addCompilationCustomizers(customImports);
	}
	
	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, TileSetLoaderParameters parameter) {
		
		//
		// Load the specified image for each tile-descriptor,
		// create the resulting TiledMapTile instance,
		// and insert it into the TileSet.
		//
		for (Tile t : tileSet.getAllTiles()) {
			
			final FileHandle imageFile = file.parent().child(t.getFilename());
			if (!imageFile.exists())
				throw new RuntimeException(new FileNotFoundException("Cannot load tile-set \"" + file.path()
						+ "\" -- referenced image-file \"" + imageFile.path() + "\" does not exist."));
			
			manager.load(imageFile.path(), Texture.class);
			manager.finishLoadingAsset(imageFile.path());
			final Texture imageTexture = manager.get(imageFile.path(), Texture.class);
			
			//
			// Calculate safe texture-region dimensions, clamping the specified to the
			// actual texture-dimensions.
			
			if (t.getWidth() == 0)
				t.setWidth(imageTexture.getWidth() - 2 * t.getPadding());
			if (t.getHeight() == 0)
				t.setHeight(imageTexture.getHeight() - 2 * t.getPadding());
			
			final int startX = min(max(0, t.getX() + t.getPadding()), imageTexture.getWidth() - 1);
			final int startY = min(max(0, t.getY() + t.getPadding()), imageTexture.getHeight() - 1);
			final int width = max(min(t.getWidth(), imageTexture.getWidth() - t.getPadding()), 0);
			final int height = max(min(t.getHeight(), imageTexture.getHeight() - t.getPadding()), 0);
			
			final TextureRegion imageRegion = new TextureRegion(imageTexture, startX, startY, width, height);
			t.setSprite(imageRegion);
			
		}
	}
	
	@Override
	public TileSet loadSync(AssetManager manager, String fileName, FileHandle file, TileSetLoaderParameters parameter) {
		
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
		
		final TileSetDsl dsl = new TileSetDsl();
		final DelegatingScript script;
		try {
			
			final GroovyShell shell = new GroovyShell(this.getClass().getClassLoader(), new Binding(), config);
			script = (DelegatingScript) shell.parse(file.file());
			script.setDelegate(dsl);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		try {
			
			script.run();
			tileSet = dsl.build();
			
			for (Tile td : tileSet.getAllTiles()) {
				dependencies.add(new AssetDescriptor<>(file.parent().child(td.getFilename()), Texture.class));
			}
			
		} catch (Throwable t) {
			LOG.error(t, "Cannot load tile-set \"{0}\" -- unexpected exception.", file.path());
			throw new RuntimeException("Cannot load tile-set \"" + file.path() + "\" -- unexpected exception.", t);
		}
		
		return dependencies;
	}
	
	public static class TileSetLoaderParameters extends AssetLoaderParameters<TileSet> {
		
	}
}
