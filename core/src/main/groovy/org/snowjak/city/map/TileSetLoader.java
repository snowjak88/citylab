/**
 * 
 */
package org.snowjak.city.map;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.snowjak.city.map.TileSetLoader.TileSetLoaderParameters;

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
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

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
	
	private static final Json JSON = new Json(OutputType.javascript);
	{
		JSON.setSerializer(TileSetDescriptor.class, new TileSetDescriptorSerializer());
	}
	
	private TileSetDescriptor tileSetDescriptor;
	
	/**
	 * Construct a new {@link TileSetLoader}.
	 * 
	 * @param resolver
	 */
	public TileSetLoader(FileHandleResolver resolver) {
		
		super(resolver);
	}
	
	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, TileSetLoaderParameters parameter) {
		
		//
		// do nothing
	}
	
	@Override
	public TileSet loadSync(AssetManager manager, String fileName, FileHandle file, TileSetLoaderParameters parameter) {
		
		final TileSet tileset = new TileSet(file, tileSetDescriptor);
		final FileHandle tileSetBaseDirectory = file.parent();
		
		for (TileDescriptor tileDescriptor : tileSetDescriptor.getAllTiles()) {
			final FileHandle textureFile = tileSetBaseDirectory.child(tileDescriptor.getFilename());
			final Texture tileTexture = manager.get(textureFile.path(), Texture.class);
			
			final int startX = min(max(tileDescriptor.getX() + tileDescriptor.getPadding(), 0),
					tileTexture.getWidth() - 1);
			final int startY = min(max(tileDescriptor.getY() + tileDescriptor.getPadding(), 0),
					tileTexture.getHeight() - 1);
			final int width = max(
					min(tileDescriptor.getWidth(), tileTexture.getWidth() - 1 - tileDescriptor.getPadding() - startX),
					0);
			final int height = max(min(tileDescriptor.getHeight(),
					tileTexture.getHeight() - 1 - tileDescriptor.getPadding() - tileDescriptor.getY()), 0);
			
			final TextureRegion tileTextureRegion = new TextureRegion(tileTexture, startX, startY, width, height);
			
			final TiledMapTile tile = new StaticTiledMapTile(tileTextureRegion);
			tile.setId(tileDescriptor.getHashcode());
			tile.setOffsetY(tileDescriptor.getOffset());
			tile.getProperties().put(PROPERTIES_TILE_TITLE, tileDescriptor.getTitle());
			
			tileset.putTile(tileDescriptor.getHashcode(), tile);
		}
		
		tileset.getProperties().put(PROPERTIES_TILESET_TITLE, tileSetDescriptor.getTitle());
		tileset.getProperties().put(PROPERTIES_TILESET_FILENAME, file.path());
		
		return tileset;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, TileSetLoaderParameters parameter) {
		
		LOG.debug("Identifying dependencies for {0}", fileName);
		
		tileSetDescriptor = JSON.fromJson(TileSetDescriptor.class, file);
		
		final Set<String> imageFilenames = new HashSet<>();
		for (TileDescriptor tile : tileSetDescriptor.getAllTiles())
			imageFilenames.add(file.parent().child(tile.getFilename()).path());
		
		final Array<AssetDescriptor> requiredImages = new Array<>();
		imageFilenames.stream().map(f -> new AssetDescriptor<>(f, Texture.class)).forEach(requiredImages::add);
		
		LOG.debug("Listed dependencies: {0}", imageFilenames.stream().collect(Collectors.joining(", ")));
		
		return requiredImages;
	}
	
	public static class TileSetLoaderParameters extends AssetLoaderParameters<TileSet> {
		
	}
}
