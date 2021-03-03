/**
 * 
 */
package org.snowjak.city.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.utils.IntMap;

/**
 * Describes an entire Tile-Set.
 * 
 * @author snowjak88
 *
 */
public class TileSetDescriptor {
	
	private static final Random RND = new Random(System.currentTimeMillis());
	private static final ArrayList<TileDescriptor> EMPTY_LIST = new ArrayList<>();
	
	private final String title, description;
	private final int width, height, gridWidth, gridHeight, offset, padding;
	private final Collection<TileDescriptor> allTiles = new LinkedList<>();
	private final Map<String, ArrayList<TileDescriptor>> tilesByTitle = new HashMap<>();
	private final IntMap<TileDescriptor> tilesByHash = new IntMap<>();
	
	public TileSetDescriptor(String title, String description, int width, int height, int gridWidth, int gridHeight,
			int offset, int padding) {
		
		this.title = title;
		this.description = description;
		this.width = width;
		this.height = height;
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.offset = offset;
		this.padding = padding;
	}
	
	public String getTitle() {
		
		return title;
	}
	
	public String getDescription() {
		
		return description;
	}
	
	public int getWidth() {
		
		return width;
	}
	
	public int getHeight() {
		
		return height;
	}
	
	public int getGridWidth() {
		
		return gridWidth;
	}
	
	public int getGridHeight() {
		
		return gridHeight;
	}
	
	public int getOffset() {
		
		return offset;
	}
	
	public int getPadding() {
		
		return padding;
	}
	
	public void addTile(TileDescriptor tile) {
		
		assert (tile != null);
		
		allTiles.add(tile);
		tile.getTitle();
		tilesByTitle.computeIfAbsent(tile.getTitle(), (t) -> new ArrayList<>()).add(tile);
		
		tilesByHash.put(tile.getHashcode(), tile);
	}
	
	public Collection<TileDescriptor> getAllTilesByTitle(String tileTitle) {
		
		return Collections.unmodifiableCollection(tilesByTitle.getOrDefault(tileTitle, EMPTY_LIST));
	}
	
	public Collection<TileDescriptor> getAllTiles() {
		
		return Collections.unmodifiableCollection(allTiles);
	}
	
	/**
	 * Get the {@link TileDescriptor} corresponding to the given {@code hashcode}
	 * (derived from {@link TileDescriptor#getHashcode()}), or {@code null} if a
	 * TileDescriptor with that hashcode has not been imported as part of this
	 * {@link TileSetDescriptor}.
	 * 
	 * @param hashcode
	 * @return
	 */
	public TileDescriptor getTileByHashcode(int hashcode) {
		
		return tilesByHash.get(hashcode);
	}
	
	/**
	 * Get a {@link TileDescriptor} associated with the given ID, or {@code null} if
	 * none so registered. If more than one {@link TileDescriptor} is registered
	 * under this ID, selects the first one from the list.
	 * 
	 * @param tileTitle
	 * @return
	 */
	public TileDescriptor getTileByTitle(String tileTitle) {
		
		if (!tilesByTitle.containsKey(tileTitle))
			return null;
		
		final ArrayList<TileDescriptor> availableTiles = tilesByTitle.get(tileTitle);
		if (availableTiles.isEmpty())
			return null;
		return availableTiles.get(0);
	}
	
	/**
	 * Get a {@link TileDescriptor} associated with the given ID, or {@code null} if
	 * none so registered. If more than one {@link TileDescriptor} is registered
	 * under this ID, selects one at random.
	 * 
	 * @param tileTitle
	 * @return
	 */
	public TileDescriptor getRandomTileByTitle(String tileTitle) {
		
		if (!tilesByTitle.containsKey(tileTitle))
			return null;
		
		final ArrayList<TileDescriptor> availableTiles = tilesByTitle.get(tileTitle);
		if (availableTiles.isEmpty())
			return null;
		if (availableTiles.size() == 1)
			return availableTiles.get(0);
		
		return availableTiles.get(RND.nextInt(availableTiles.size()));
	}
}
