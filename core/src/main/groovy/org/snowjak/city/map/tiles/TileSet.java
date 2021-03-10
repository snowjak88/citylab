/**
 * 
 */
package org.snowjak.city.map.tiles;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

import org.snowjak.city.map.tiles.TileDescriptor.Corner;
import org.snowjak.city.map.tiles.TileDescriptor.CornerFilter;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.utils.OrderedMap;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

/**
 * @author snowjak88
 *
 */
public class TileSet extends TiledMapTileSet {
	
	private static final Logger LOG = LoggerService.forClass(TileSet.class);
	
	private final FileHandle descriptorFile;
	private final TileSetDescriptor descriptor;
	
	private final OrderedMap<TileDescriptor, TiledMapTile> tileDescriptorsToTiles = new OrderedMap<>();
	private final OrderedMap<TiledMapTile, TileDescriptor> tilesToTileDescriptors = new OrderedMap<>();
	
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
	
	/**
	 * Attempt to merge the given TileSet with this TileSet, producing a third
	 * TileSet.
	 * <p>
	 * If the merge is unsuccessful, throws an {@link IllegalArgumentException}.
	 * </p>
	 * 
	 * @param toMerge
	 * @return
	 * @see TileSetDescriptor#merge(TileSetDescriptor)
	 */
	public TileSet merge(TileSet toMerge) throws IllegalArgumentException {
		
		final TileSetDescriptor mergedDescriptor = this.descriptor.merge(toMerge.descriptor);
		final Map<Integer, TiledMapTile> tiles = new LinkedHashMap<>();
		
		this.tilesToTileDescriptors.forEach(e -> tiles.put(e.value.getHashcode(), e.key));
		toMerge.tilesToTileDescriptors.forEach(e -> tiles.put(e.value.getHashcode(), e.key));
		
		final TileSet mergedTileSet = new TileSet(null, mergedDescriptor);
		tiles.forEach((i, t) -> mergedTileSet.putTile(i, t));
		
		return mergedTileSet;
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
	
	public TiledMapTile getTileByDescriptor(TileDescriptor tileDescriptor) {
		
		assert (tileDescriptor != null);
		return tileDescriptorsToTiles.get(tileDescriptor);
	}
	
	/**
	 * 
	 * @param altitude
	 *            Array of altitude measurements at each tile-corner intersection.
	 *            If {@code null} or smaller than [2x2], will not figure in
	 *            tile-filtering.
	 * @param x
	 *            the location for which to filter
	 * @param y
	 * @param wrapX
	 * @param wrapY
	 * @param material
	 *            If {@code null}, [material] will not figure in tile-filtering
	 * @return
	 */
	public Collection<TileDescriptor> findDescriptorsThatFit(int[][] altitude, int x, int y, boolean wrapX,
			boolean wrapY, String[][] material) {
		
		if (altitude == null && material == null)
			return findDescriptorsThatFit(null, null);
		
		final int width = (altitude == null) ? material.length : altitude.length;
		final int height = (altitude == null) ? material[0].length : altitude[0].length;
		
		if (width < 2 || height < 2)
			return findDescriptorsThatFit(null, null);
		
		final int minX = x, maxX = x + 1;
		final int minY = y, maxY = y + 1;
		
		final int[][] localAltitude = new int[2][2];
		final String[][] localMaterial = new String[2][2];
		for (int nx = minX, lx = 0; nx <= maxX; nx++, lx++) {
			final boolean overflowX = (nx < 0 || nx >= width);
			for (int ny = minY, ly = 0; ny <= maxY; ny++, ly++) {
				final boolean overflowY = (ny < 0 || ny >= height);
				final boolean useCenter = (overflowX && !wrapX) || (overflowY && !wrapY);
				localAltitude[lx][ly] = (useCenter) ? altitude[x][y]
						: altitude[wrap(nx, 0, width - 1)][wrap(ny, 0, height - 1)];
				localMaterial[lx][ly] = (useCenter) ? material[x][y]
						: material[wrap(nx, 0, width - 1)][wrap(ny, 0, height - 1)];
			}
		}
		
		return findDescriptorsThatFit(localAltitude, localMaterial);
	}
	
	/**
	 * 
	 * @param surrounding
	 *            Expecting a 2x2 array representing altitudes at the tile's
	 *            corners. If {@code null}, altitude-deltas will not figure in
	 *            tile-filtering
	 * @param onlyMaterial
	 *            Expecting a 2x2 array representing material-IDs at the tile's
	 *            corners. If (@code null), material-IDs will not figure in
	 *            tile-filtering
	 * @return
	 */
	public Collection<TileDescriptor> findDescriptorsThatFit(int[][] surrounding, String[][] onlyMaterial) {
		
		final Set<TileDescriptor> results = new LinkedHashSet<>();
		tileDescriptorsToTiles.keys().forEach(results::add);
		
		results.removeIf(td -> !isTileFitting(0, 0, td, surrounding, onlyMaterial, false, false));
		
		return results;
	}
	
	/**
	 * Does the TileDescriptor located at {@code x,y} "fit" within the descriptors
	 * surrounding it?
	 * 
	 * @param x
	 * @param y
	 * @param descriptor
	 * @param altitude
	 * @param material
	 * @param wrapX
	 * @param wrapY
	 * @return
	 */
	public boolean isTileFitting(int x, int y, TileDescriptor descriptor, int[][] altitude, String[][] material,
			boolean wrapX, boolean wrapY) {
		
		if (altitude == null && material == null)
			return false;
		
		final int width = (altitude == null) ? material.length : altitude.length;
		final int height = (altitude == null) ? material[0].length : altitude[0].length;
		final TileDescriptor[][] descriptors = new TileDescriptor[width - 1][height - 1];
		descriptors[x][y] = descriptor;
		return isTileFitting(x, y, descriptors, altitude, material, wrapX, wrapY);
	}
	
	public boolean isTileFitting(int x, int y, TileDescriptor[][] descriptors, int[][] altitude, String[][] material,
			boolean wrapX, boolean wrapY) {
		
		if (((x < 0 || x >= descriptors.length) && !wrapX) || ((y < 0 || y >= descriptors[0].length) && !wrapY))
			return false;
		
		x = wrap(x, 0, descriptors.length - 1);
		y = wrap(y, 0, descriptors[0].length - 1);
		
		final int[][] normalizedDeltas = new int[2][2];
		if (altitude != null) {
			//
			// Normalize altitude-deltas from [x,y] to [x+1,y+1]
			
			// final int[][] normalizedDeltas = new int[2][2];
			int minAlt = Integer.MAX_VALUE;
			for (int dx = 0; dx < 2; dx++) {
				if ((x + dx < 0 || x + dx >= altitude.length) && !wrapX)
					continue;
				
				for (int dy = 0; dy < 2; dy++) {
					if ((y + dy < 0 || y + dy >= altitude[0].length) && !wrapY)
						continue;
					
					minAlt = Math.min(minAlt,
							altitude[wrap(x + dx, 0, altitude.length - 1)][wrap(y + dy, 0, altitude[0].length - 1)]);
				}
			}
			for (int dx = 0; dx < 2; dx++) {
				if ((x + dx < 0 || x + dx >= altitude.length) && !wrapX)
					continue;
				
				for (int dy = 0; dy < 2; dy++) {
					if ((y + dy < 0 || y + dy >= altitude[0].length) && !wrapY)
						continue;
					
					normalizedDeltas[dx][dy] = altitude[wrap(x + dx, 0, altitude.length - 1)][wrap(y + dy, 0,
							altitude[0].length - 1)] - minAlt;
				}
			}
			
			//
			// Normalize this tile's corner altitude-deltas.
			//
			final OptionalInt[][] normalizedExpectedDeltas = new OptionalInt[2][2];
			OptionalInt minExpectedDelta = OptionalInt.empty();
			
			for (Corner thisCorner : Corner.values()) {
				final CornerFilter filter = descriptors[x][y].getCornerFilter(thisCorner);
				if (!filter.getAltitudeDelta().isPresent())
					continue;
				if (!minExpectedDelta.isPresent())
					minExpectedDelta = filter.getAltitudeDelta();
				else
					minExpectedDelta = OptionalInt
							.of(Math.min(minExpectedDelta.getAsInt(), filter.getAltitudeDelta().getAsInt()));
			}
			
			//
			// No corner had an expected delta.
			if (!minExpectedDelta.isPresent())
				return true;
				
			//
			// Normalize the expected deltas.
			for (Corner thisCorner : Corner.values()) {
				final int cx = Math.max(0, thisCorner.getDx());
				final int cy = Math.max(0, thisCorner.getDy());
				final CornerFilter filter = descriptors[x][y].getCornerFilter(thisCorner);
				if (!filter.getAltitudeDelta().isPresent())
					normalizedExpectedDeltas[cx][cy] = filter.getAltitudeDelta();
				else
					normalizedExpectedDeltas[cx][cy] = OptionalInt
							.of(filter.getAltitudeDelta().getAsInt() - minExpectedDelta.getAsInt());
			}
			
			//
			// Compare the expected vs the actual deltas.
			for (int dx = 0; dx < 2; dx++)
				for (int dy = 0; dy < 2; dy++)
					if (normalizedExpectedDeltas[dx][dy].isPresent()
							&& normalizedExpectedDeltas[dx][dy].getAsInt() != normalizedDeltas[dx][dy])
						return false;
		}
		
		//
		// Compare material-IDs across material[][] constraints
		if (material != null) {
			for (Corner thisCorner : Corner.values()) {
				
				final int dx = (thisCorner.getDx() < 0) ? 0 : 1;
				final int dy = (thisCorner.getDy() < 0) ? 0 : 1;
				int mx = x + dx;
				int my = y + dy;
				
				final boolean overflowX = (mx >= material.length);
				final boolean overflowY = (my >= material[0].length);
				
				if ((overflowX && !wrapX) || (overflowY && !wrapY))
					continue;
				
				mx = wrap(mx, 0, material.length - 1);
				my = wrap(my, 0, material[0].length - 1);
				
				final Set<String> validMaterials = descriptors[x][y].getCornerFilter(thisCorner).getValidMaterials();
				if (validMaterials == null || validMaterials.isEmpty())
					continue;
				
				for (String thisMaterial : validMaterials)
					if (material[mx][my] != null && !material[mx][my].isEmpty()
							&& !material[mx][my].equalsIgnoreCase(thisMaterial))
						return false;
			}
		}
		
		//
		// Compare material- and flavor-IDs across adjoining TileDescriptors
		for (Corner thisCorner : Corner.values()) {
			final CornerFilter thisCornerFilter = descriptors[x][y].getCornerFilter(thisCorner);
			
			if (thisCornerFilter.getValidMaterials() == null || thisCornerFilter.getValidMaterials().isEmpty())
				continue;
				
			//
			// Consider all adjoining corners.
			for (Corner otherCorner : Corner.values()) {
				if (thisCorner == otherCorner)
					continue;
				
				int nX = (int) ((float) x + (float) thisCorner.getDx() / 2.0 - (float) otherCorner.getDx() / 2.0);
				int nY = (int) ((float) y + (float) thisCorner.getDy() / 2.0 - (float) otherCorner.getDy() / 2.0);
				
				if (nX == x && nY == y)
					continue;
				
				if (((nX < 0 || nX >= descriptors.length) && !wrapX)
						|| ((nY < 0 || nY >= descriptors[0].length) && !wrapY))
					continue;
					
				//
				// Get the corner-filter from the specified neighbor
				final TileDescriptor neighbor = descriptors[wrap(nX, 0, descriptors.length - 1)][wrap(nY, 0,
						descriptors[0].length - 1)];
				if (neighbor == null)
					continue;
				final CornerFilter neighborFilter = neighbor.getCornerFilter(otherCorner);
				
				//
				// If the corners do not share any Materials, that's a failure.
				// If the corners *do* share a Material, but that Material has no shared
				// flavors, that's a failure.
				// (of course, if a material has *no* valid-flavor list, it matches with *all*
				// valid-flavors)
				final boolean anyMaterialFlavorMatch =
				//@formatter:off
						thisCornerFilter.getValidMaterials().stream()
							.anyMatch(
									m -> neighborFilter.getValidMaterials().contains(m)
										&& (
												thisCornerFilter.getValidFlavorsFor(m) == null
												|| thisCornerFilter.getValidFlavorsFor(m).isEmpty()
												|| neighborFilter.getValidFlavorsFor(m) == null
												|| neighborFilter.getValidFlavorsFor(m).isEmpty()
												|| thisCornerFilter.getValidFlavorsFor(m).stream()
													.anyMatch(f -> neighborFilter.getValidFlavorsFor(m).contains(f))
											));
				//@formatter:on
				if (!anyMaterialFlavorMatch)
					return false;
			}
		}
		
		return true;
		
	}
	
	private int wrap(int v, int min, int max) {
		
		if (v >= min && v <= max)
			return v;
		
		final int range = (max + 1) - min;
		while (v < min)
			v += range;
		while (v > max)
			v -= range;
		return v;
	}
}
