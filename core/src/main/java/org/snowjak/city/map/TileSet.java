/**
 * 
 */
package org.snowjak.city.map;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.OrderedMap;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

/**
 * @author snowjak88
 *
 */
public class TileSet extends TiledMapTileSet {
	
	private static final Logger LOG = LoggerService.forClass(TileSet.class);
	
	private static final Json JSON = new Json(OutputType.javascript);
	{
		JSON.setSerializer(TileSetDescriptor.class, new TileSetDescriptorSerializer());
	}
	
	private final FileHandle descriptorFile;
	private final TileSetDescriptor descriptor;
	
	private final OrderedMap<TileDescriptor, TiledMapTile> tileDescriptorsToTiles = new OrderedMap<>();
	private final OrderedMap<TiledMapTile, TileDescriptor> tilesToTileDescriptors = new OrderedMap<>();
	
	/**
	 * Construct a new {@link TileSet} from the given file (expected to
	 * {@link TileSetDescriptorSerializer de-serialize} to a
	 * {@link TileSetDescriptor}).
	 * 
	 * @param tilesetDescriptorFile
	 */
	public TileSet(FileHandle tilesetDescriptorFile) {
		
		this(tilesetDescriptorFile, JSON.fromJson(TileSetDescriptor.class, tilesetDescriptorFile));
	}
	
	/**
	 * Construct a new {@link TileSet} using the given {@link TileSetDescriptor}.
	 * This descriptor is assumed to have been imported from the given
	 * {@code descriptorFile}.
	 * 
	 * @param descriptorFile
	 *            must not be {@code null}
	 * @param tilesetDescriptor
	 *            must not be {@code null}
	 */
	public TileSet(FileHandle descriptorFile, TileSetDescriptor tilesetDescriptor) {
		
		super();
		
		assert (descriptorFile != null);
		assert (tilesetDescriptor != null);
		
		this.descriptorFile = descriptorFile;
		this.descriptor = tilesetDescriptor;
		
		setName(tilesetDescriptor.getTitle());
	}
	
	@Override
	public void putTile(int id, TiledMapTile tile) {
		
		super.putTile(id, tile);
		
		final TileDescriptor tileDescriptor = descriptor.getTileByHashcode(id);
		
		assert (tileDescriptor != null);
		
		tilesToTileDescriptors.put(tile, tileDescriptor);
		tileDescriptorsToTiles.put(tileDescriptor, tile);
	}
	
	public FileHandle getDescriptorFile() {
		
		return descriptorFile;
	}
	
	public TileSetDescriptor getTileSetDescriptor() {
		
		return descriptor;
	}
	
	public TileDescriptor getTileDescriptor(int id) {
		
		return descriptor.getTileByHashcode(id);
	}
	
	public TiledMapTile getRandomTile(String title) {
		
		return getTileByDescriptor(descriptor.getRandomTileByTitle(title));
	}
	
	public TiledMapTile getTile(String title) {
		
		return getTileByDescriptor(descriptor.getTileByTitle(title));
	}
	
	public TiledMapTile getTileByDescriptor(TileDescriptor tileDescriptor) {
		
		assert (tileDescriptor != null);
		return tileDescriptorsToTiles.get(tileDescriptor);
	}
}
