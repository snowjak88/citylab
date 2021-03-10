/**
 * 
 */
package org.snowjak.city.map.tiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
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
	
	public TileSetDescriptor(TileSetDescriptor toCopy) {
		
		this(toCopy.title, toCopy.description, toCopy.width, toCopy.height, toCopy.gridWidth, toCopy.gridHeight,
				toCopy.offset, toCopy.padding);
		toCopy.allTiles.forEach(this::addTile);
	}
	
	/**
	 * Attempt to merge this TileSetDescriptor with another, producing a third
	 * TileSetDescriptor.
	 * <p>
	 * This method will fail (producing an {@link IllegalArgumentException}) if:
	 * <ul>
	 * <li>Grid-sizes are not identical
	 * ({@link #getGridWidth()}/{@link #getGridHeight()}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param other
	 * @return
	 */
	public TileSetDescriptor merge(TileSetDescriptor other) throws IllegalArgumentException {
		
		final TileSetDescriptor copy = new TileSetDescriptor(this);
		if (other == null)
			return copy;
		
		if (this.getGridWidth() != other.getGridWidth() || this.getGridHeight() != other.getGridHeight())
			throw new IllegalArgumentException("Cannot merge TileSetDescriptors with mismatched grid dimensions.");
		
		other.allTiles.forEach(copy::addTile);
		
		return copy;
	}
	
	public String getTitle() {
		
		return title;
	}
	
	public String getDescription() {
		
		return description;
	}
	
	int getWidth() {
		
		return width;
	}
	
	int getHeight() {
		
		return height;
	}
	
	public int getGridWidth() {
		
		return gridWidth;
	}
	
	public int getGridHeight() {
		
		return gridHeight;
	}
	
	int getOffset() {
		
		return offset;
	}
	
	int getPadding() {
		
		return padding;
	}
	
	public void addTile(TileDescriptor tile) {
		
		assert (tile != null);
		
		allTiles.add(tile);
		tilesByHash.put(tile.getHashcode(), tile);
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
}
