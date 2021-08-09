/**
 * 
 */
package org.snowjak.city.map.tiles;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.snowjak.city.map.CityMap;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.LongMap;

/**
 * Holds tiles.
 * 
 * @author snowjak88
 *
 */
public class TileSet implements Disposable {
	
	private final String title, description;
	private final int width, height, gridWidth, gridHeight, surfaceOffset, altitudeOffset, padding;
	private final Collection<Tile> allTiles = new LinkedList<>();
	private final LongMap<Tile> tilesByHash = new LongMap<>();
	
	private final List<MapMutator> mutators = new LinkedList<>();
	
	public TileSet(String title, String description, int width, int height, int gridWidth, int gridHeight,
			int surfaceOffset, int altitudeOffset, int padding, Collection<MapMutator> mutators) {
		
		this.title = title;
		this.description = description;
		this.width = width;
		this.height = height;
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.surfaceOffset = surfaceOffset;
		this.altitudeOffset = altitudeOffset;
		this.padding = padding;
		this.mutators.addAll(mutators);
	}
	
	public TileSet(TileSet toCopy) {
		
		this(toCopy.title, toCopy.description, toCopy.width, toCopy.height, toCopy.gridWidth, toCopy.gridHeight,
				toCopy.surfaceOffset, toCopy.altitudeOffset, toCopy.padding, toCopy.mutators);
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
	public TileSet merge(TileSet other) throws IllegalArgumentException {
		
		final TileSet copy = new TileSet(this);
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
	
	int getSurfaceOffset() {
		
		return surfaceOffset;
	}
	
	public int getAltitudeOffset() {
		
		return altitudeOffset;
	}
	
	int getPadding() {
		
		return padding;
	}
	
	public void addTile(Tile tile) {
		
		assert (tile != null);
		
		allTiles.add(tile);
		tilesByHash.put(tile.getHashcode(), tile);
	}
	
	public Collection<Tile> getAllTiles() {
		
		return Collections.unmodifiableCollection(allTiles);
	}
	
	/**
	 * Get the {@link Tile} corresponding to the given {@code hashcode} (derived
	 * from {@link Tile#getHashcode()}), or {@code null} if a TileDescriptor with
	 * that hashcode has not been imported as part of this {@link TileSet}.
	 * 
	 * @param hashcode
	 * @return
	 */
	public Tile getTileByHashcode(long hashcode) {
		
		return tilesByHash.get(hashcode);
	}
	
	/**
	 * Get the {@link MapMutator}s associated with this TileSet.
	 * 
	 * @return
	 */
	public List<MapMutator> getMutators() {
		
		return Collections.unmodifiableList(mutators);
	}
	
	/**
	 * Execute this TileSet's configured {@link MapMutator}s against the given map
	 * for the given cell.
	 * 
	 * @param map
	 * @param cellX
	 * @param cellY
	 */
	public void mutate(CityMap map, int cellX, int cellY) {
		
		mutators.forEach(m -> m.mutate(map, cellX, cellY));
	}
	
	/**
	 * Get the minimum set of Tiles that can fit the given map at the given
	 * location.
	 * <p>
	 * You should probably execute {@link #mutate(CityMap, int, int) mutate()}
	 * against this location before attempting to look for fitting tiles.
	 * </p>
	 * <p>
	 * Note that this should only look at vertex-altitudes and -flavors (barring any
	 * special Tile rules, which would be executed normally).
	 * </p>
	 */
	public List<Tile> getMinimalTilesFor(CityMap map, int cellX, int cellY) {
		
		final EnumMap<TileCorner, List<String>> remainingFlavors = new EnumMap<>(TileCorner.class);
		for (TileCorner corner : TileCorner.values())
			remainingFlavors.put(corner, new LinkedList<String>(map.getTileCornerFlavors(cellX, cellY, corner)));
		
		return searchMinimalTilesFor(map, cellX, cellY, remainingFlavors, new HashSet<Tile>(), true);
	}
	
	private List<Tile> searchMinimalTilesFor(CityMap map, int cellX, int cellY,
			EnumMap<TileCorner, List<String>> remainingFlavors, Set<Tile> currentTiles, boolean nonDecorative) {
		
		boolean allDone = true;
		for (TileCorner corner : remainingFlavors.keySet()) {
			if (!remainingFlavors.get(corner).isEmpty()) {
				allDone = false;
				break;
			}
		}
		
		if (allDone)
			return Collections.emptyList();
		
		List<Tile> bestTileList = null;
		List<Tile> currentTileList = new LinkedList<>();
		
		//
		// Consider each tile in order.
		//
		for (Tile tile : allTiles) {
			
			//
			// If the tile is non-transparent and we're not allowing that -- skip it.
			if (nonDecorative == tile.isDecoration())
				continue;
				
			//
			// If we've already added this tile to the current chain -- skip it.
			if (currentTiles.contains(tile))
				continue;
				
			//
			// If the tile's rules don't allow it to fit here -- skip it.
			if (!tile.isAcceptable(map, cellX, cellY))
				continue;
				
			//
			// OK -- now get ready to go a level deeper.
			//
			// Prepare the list that will receive the results of our search.
			//
			if (currentTileList == null)
				currentTileList = new LinkedList<>();
			else
				currentTileList.clear();
			currentTileList.add(tile);
			
			//
			// The "new remaining" list of flavors is the old list, minus the
			// currently-selected tile's flavors
			final EnumMap<TileCorner, List<String>> newRemaining = new EnumMap<>(TileCorner.class);
			for (TileCorner corner : remainingFlavors.keySet()) {
				final List<String> remaining = new LinkedList<String>(remainingFlavors.get(corner));
				remaining.removeAll(tile.getProvision().get(corner));
				newRemaining.put(corner, remaining);
			}
			
			//
			// We don't want to select the current tile again in our search.
			currentTiles.add(tile);
			
			//
			// Do the search and capture the results in the list.
			final List<Tile> searchResult = searchMinimalTilesFor(map, cellX, cellY, newRemaining, currentTiles, false);
			if (searchResult != null)
				currentTileList.addAll(searchResult);
			
			currentTiles.remove(tile);
			
			//
			// If we haven't chosen a best list yet,
			// or if the current list is shorter than our best,
			// make the current our best.
			if (bestTileList == null || bestTileList.size() > currentTileList.size()) {
				bestTileList = currentTileList;
				currentTileList = null;
			}
		}
		
		return bestTileList;
	}
	
	@Override
	public void dispose() {
		
		allTiles.forEach(t -> t.dispose());
	}
}