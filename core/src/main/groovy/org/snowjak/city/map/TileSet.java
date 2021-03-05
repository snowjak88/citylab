/**
 * 
 */
package org.snowjak.city.map;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import org.snowjak.city.map.TileDescriptor.Corner;
import org.snowjak.city.map.TileDescriptor.CornerFilter;

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
	
	public Collection<TileDescriptor> getAllDescriptorsFor(String title) {
		
		return descriptor.getAllTilesByTitle(title);
	}
	
	public Collection<TiledMapTile> getAllTilesFor(String title) {
		
		return getAllDescriptorsFor(title).stream().map(td -> getTileByDescriptor(td)).collect(Collectors.toSet());
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
	 * @param onlyTitle
	 *            If {@code null}, [title] will not figure in tile-filtering
	 * @return
	 */
	public Collection<TileDescriptor> findDescriptorsThatFit(int[][] altitude, int x, int y, boolean wrapX,
			boolean wrapY, String onlyTitle) {
		
		if (altitude == null)
			return findDescriptorsThatFit(null, onlyTitle);
		
		final int width = altitude.length, height = altitude[0].length;
		
		if (width < 2 || height < 2)
			return findDescriptorsThatFit(null, onlyTitle);
		
		final int minX = x, maxX = x + 1;
		final int minY = y, maxY = y + 1;
		
		final int[][] localAltitude = new int[2][2];
		for (int nx = minX, lx = 0; nx <= maxX; nx++, lx++) {
			final boolean overflowX = (nx < 0 || nx >= width);
			for (int ny = minY, ly = 0; ny <= maxY; ny++, ly++) {
				final boolean overflowY = (ny < 0 || ny >= height);
				final boolean useCenterAlt = (overflowX && !wrapX) || (overflowY && !wrapY);
				if (useCenterAlt)
					localAltitude[lx][ly] = altitude[x][y];
				else
					localAltitude[lx][ly] = altitude[wrap(nx, 0, width - 1)][wrap(ny, 0, height - 1)];
			}
		}
		
		return findDescriptorsThatFit(localAltitude, onlyTitle);
	}
	
	/**
	 * 
	 * @param surrounding
	 *            Expecting a 2x2 array representing altitudes at the tile's
	 *            corners. If {@code null}, altitude-deltas will not figure in
	 *            tile-filtering
	 * @param onlyTitle
	 *            If {@code null}, [title] will not figure in tile-filtering
	 * @return
	 */
	public Collection<TileDescriptor> findDescriptorsThatFit(int[][] surrounding, String onlyTitle) {
		
		final Set<TileDescriptor> results = new LinkedHashSet<>();
		tileDescriptorsToTiles.keys().forEach(results::add);
		
		if (onlyTitle != null)
			results.removeIf(td -> td.getTitle() != null && !td.getTitle().equalsIgnoreCase(onlyTitle));
		
		if (surrounding != null && surrounding.length == 2 && surrounding[0].length == 2)
			results.removeIf(td -> !isTileFitting(0, 0, td, surrounding, false, false));
		
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
	 * @param wrapX
	 * @param wrapY
	 * @return
	 */
	public boolean isTileFitting(int x, int y, TileDescriptor descriptor, int[][] altitude, boolean wrapX,
			boolean wrapY) {
		
		final TileDescriptor[][] descriptors = new TileDescriptor[altitude.length - 1][altitude[0].length - 1];
		descriptors[x][y] = descriptor;
		return isTileFitting(x, y, descriptors, altitude, wrapX, wrapY, true);
	}
	
	public boolean isTileFitting(int x, int y, TileDescriptor[][] descriptors, int[][] altitude, boolean wrapX,
			boolean wrapY) {
		
		return isTileFitting(x, y, descriptors, altitude, wrapX, wrapY, false);
	}
	
	public boolean isTileFitting(int x, int y, TileDescriptor[][] descriptors, int[][] altitude, boolean wrapX,
			boolean wrapY, boolean onlyAltitude) {
		
		if (((x < 0 || x >= descriptors.length) && !wrapX) || ((y < 0 || y >= descriptors[0].length) && !wrapY))
			return false;
		
		x = wrap(x, 0, descriptors.length - 1);
		y = wrap(y, 0, descriptors[0].length - 1);
		
		//
		// Normalize altitude-deltas from [x,y] to [x+1,y+1]
		
		final int[][] normalizedDeltas = new int[2][2];
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
					
		//
		// If we're only comparing altitude, we can go now.
		if (onlyAltitude)
			return true;
			
		//
		// Compare tile-IDs across adjoining corners.
		for (Corner thisCorner : Corner.values()) {
			final CornerFilter thisCornerFilter = descriptors[x][y].getCornerFilter(thisCorner);
			
			if (thisCornerFilter.getTileID() == null || thisCornerFilter.getTileID().isEmpty())
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
				//
				if (thisCornerFilter.getTileID() != null && neighborFilter.getTileID() != null)
					if (!thisCornerFilter.getTileID().isEmpty() && !neighborFilter.getTileID().isEmpty())
						if (!thisCornerFilter.getTileID().equalsIgnoreCase(neighborFilter.getTileID()))
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
