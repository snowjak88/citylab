/**
 * 
 */
package org.snowjak.city.map.tiles;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.groovy.util.Maps;

import com.google.common.collect.Sets;

/**
 * Describes a single tile.
 * 
 * @author snowjak88
 *
 */
public class TileDescriptor {
	
	private final TileSetDescriptor tilesetDescriptor;
	private final String id, filename;
	private final int hashcode, x, y, width, height, padding, offset;
	private final EnumMap<Corner, CornerFilter> cornerFilters = new EnumMap<>(Corner.class);
	
	public TileDescriptor(TileSetDescriptor tilesetDescriptor, String id, String filename, OptionalInt x, OptionalInt y,
			OptionalInt width, OptionalInt height, OptionalInt padding, OptionalInt offset,
			CornerFilter... cornerFilters) {
		
		this(tilesetDescriptor, id, filename, x.orElse(0), y.orElse(0), width.orElse(tilesetDescriptor.getWidth()),
				height.orElse(tilesetDescriptor.getHeight()), padding.orElse(tilesetDescriptor.getPadding()),
				offset.orElse(tilesetDescriptor.getOffset()),
				IntStream.range(0, Corner.values().length).collect(() -> new EnumMap<>(Corner.class),
						(m, i) -> m.put(Corner.values()[i],
								(cornerFilters.length - 1 >= i) ? cornerFilters[i] : CornerFilter.EMPTY_FILTER),
						EnumMap::putAll));
	}
	
	public TileDescriptor(TileSetDescriptor tilesetDescriptor, String id, String filename, int x, int y, int width,
			int height, int padding, int offset, CornerFilter... cornerFilters) {
		
		this(tilesetDescriptor, id, filename, x, y, width, height, padding, offset,
				IntStream.range(0, Corner.values().length).collect(() -> new EnumMap<>(Corner.class),
						(m, i) -> m.put(Corner.values()[i],
								(cornerFilters.length - 1 >= i) ? cornerFilters[i] : CornerFilter.EMPTY_FILTER),
						EnumMap::putAll));
	}
	
	public TileDescriptor(TileSetDescriptor tilesetDescriptor, String id, String filename, int x, int y, int width,
			int height, int padding, int offset, EnumMap<Corner, CornerFilter> cornerFilters) {
		
		this.tilesetDescriptor = tilesetDescriptor;
		this.id = id;
		this.filename = filename;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.padding = padding;
		this.offset = offset;
		
		this.hashcode = (id + filename + Integer.toString(this.x) + Integer.toString(this.y)
				+ Integer.toString(this.width) + Integer.toString(this.height) + Integer.toString(this.padding)
				+ Integer.toString(this.offset)).hashCode();
		
		this.cornerFilters.putAll(cornerFilters);
	}
	
	public TileSetDescriptor getTilesetDescriptor() {
		
		return tilesetDescriptor;
	}
	
	String getId() {
		
		return id;
	}
	
	public String getFilename() {
		
		return filename;
	}
	
	public int getHashcode() {
		
		return hashcode;
	}
	
	public int getX() {
		
		return x;
	}
	
	public int getY() {
		
		return y;
	}
	
	public int getWidth() {
		
		return width;
	}
	
	public int getHeight() {
		
		return height;
	}
	
	public int getPadding() {
		
		return padding;
	}
	
	public int getOffset() {
		
		return offset;
	}
	
	public CornerFilter getCornerFilter(Corner corner) {
		
		return cornerFilters.get(corner);
	}
	
	public static class CornerFilter {
		
		/**
		 * i.e., "accept everything, reject nothing" filter
		 */
		public static final CornerFilter EMPTY_FILTER = new CornerFilter(null);
		
		private final OptionalInt altitudeDelta;
		private final Map<String, Set<String>> flavors = new LinkedHashMap<>();
		
		public CornerFilter(String materialID) {
			
			this(materialID, new String[0]);
		}
		
		/**
		 * Construct a new CornerFilter, accepting tiles with any altitude-delta and the
		 * given material-ID.
		 * 
		 * @param materialID
		 * @param flavors...
		 *            the tile-"flavors" to match on
		 */
		public CornerFilter(String materialID, String... flavors) {
			
			this(OptionalInt.empty(), Maps.of(materialID, Sets.newHashSet(flavors)));
		}
		
		/**
		 * Construct a new CornerFilter, accepting tiles with the given altitude-delta
		 * and material-ID
		 * 
		 * @param altitudeDelta
		 * @param materialID
		 * @param flavors
		 *            the tile-"flavors" to match on
		 */
		public CornerFilter(int altitudeDelta, String materialID, String... flavors) {
			
			this(OptionalInt.of(altitudeDelta), Maps.of(materialID, Sets.newHashSet(flavors)));
		}
		
		public CornerFilter(OptionalInt altitudeDelta, Map<String, Set<String>> flavors) {
			
			this.altitudeDelta = altitudeDelta;
			this.flavors.putAll(flavors);
		}
		
		public OptionalInt getAltitudeDelta() {
			
			return altitudeDelta;
		}
		
		/**
		 * Get the set of material-IDs which this corner-filter can be matched to
		 * 
		 * @return
		 */
		public Set<String> getValidMaterials() {
			
			return flavors.keySet();
		}
		
		/**
		 * Given a material-ID, return the set of corner-"flavors" that this tile can be
		 * matched to
		 * 
		 * @param material
		 * @return
		 */
		public Set<String> getValidFlavorsFor(String material) {
			
			if (material == null)
				return Collections.emptySet();
			return flavors.getOrDefault(material, Collections.emptySet());
		}
	}
	
	public static enum Corner {
		
		/**
		 * Top of tile-diamond.
		 */
		TOP(-1, +1, 2),
		/**
		 * Right of tile-diamond.
		 */
		RIGHT(+1, +1, 3),
		/**
		 * Bottom of tile-diamond.
		 */
		BOTTOM(+1, -1, 0),
		/**
		 * Left of tile-diamond.
		 */
		LEFT(-1, -1, 1);
		
		private final int dx, dy, oppositeIndex;
		
		private Corner(int dx, int dy, int oppositeIndex) {
			
			this.dx = dx;
			this.dy = dy;
			this.oppositeIndex = oppositeIndex;
		}
		
		public int getDx() {
			
			return dx;
		}
		
		public int getDy() {
			
			return dy;
		}
		
		public Corner getOpposite() {
			
			return Corner.values()[oppositeIndex];
		}
		
		private static final Corner[][] forDelta = new Corner[][] { { Corner.BOTTOM, Corner.LEFT },
				{ Corner.RIGHT, Corner.TOP } };
		
		/**
		 * Which corner of a tile corresponds to the given delta from that tile's
		 * coordinates?
		 * 
		 * @throws IllegalArgumentException
		 *             if either {@code dx} or {@code dy} are {@code 0}
		 */
		public static Corner getForDelta(int dx, int dy) {
			
			if (dx == 0)
				throw new IllegalArgumentException("No corner corresponds to a delta/displacement-X of 0.");
			if (dy == 0)
				throw new IllegalArgumentException("No corner corresponds to a delta/displacement-Y of 0.");
			
			return forDelta[(dx < 0) ? 0 : 1][(dy < 0) ? 0 : +1];
			
		}
	}
}
