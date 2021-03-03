/**
 * 
 */
package org.snowjak.city.map;

import java.util.EnumMap;
import java.util.OptionalInt;

/**
 * Describes a single tile.
 * 
 * @author snowjak88
 *
 */
public class TileDescriptor {
	
	private final TileSetDescriptor tilesetDescriptor;
	private final String title, filename;
	private final int hashcode, x, y, width, height, padding, offset;
	private final EnumMap<Corner, CornerFilter> cornerFilters = new EnumMap<>(Corner.class);
	
	public TileDescriptor(TileSetDescriptor tilesetDescriptor, String title, String filename, OptionalInt x,
			OptionalInt y, OptionalInt width, OptionalInt height, OptionalInt padding, OptionalInt offset,
			CornerFilter... cornerFilters) {
		
		this.tilesetDescriptor = tilesetDescriptor;
		this.title = title;
		this.filename = filename;
		this.x = x.orElse(0);
		this.y = y.orElse(0);
		this.width = width.orElse(tilesetDescriptor.getWidth());
		this.height = height.orElse(tilesetDescriptor.getHeight());
		this.padding = padding.orElse(tilesetDescriptor.getPadding());
		this.offset = offset.orElse(tilesetDescriptor.getOffset());
		
		this.hashcode = (filename + Integer.toString(this.x) + Integer.toString(this.y) + Integer.toString(this.width)
				+ Integer.toString(this.height) + Integer.toString(this.padding) + Integer.toString(this.offset))
						.hashCode();
		
		for (int i = 0; i < Corner.values().length; i++)
			this.cornerFilters.put(Corner.values()[i],
					(i >= cornerFilters.length) ? CornerFilter.EMPTY_FILTER : cornerFilters[i]);
	}
	
	public TileSetDescriptor getTilesetDescriptor() {
		
		return tilesetDescriptor;
	}
	
	public String getTitle() {
		
		return title;
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
		private final String tileID;
		
		/**
		 * Construct a new CornerFilter, accepting tiles with the given altitude-delta
		 * and tile-ID
		 * 
		 * @param altitudeDelta
		 * @param tileID
		 *            {@code null} for no tile-ID filter
		 */
		public CornerFilter(int altitudeDelta, String tileID) {
			
			this(OptionalInt.of(altitudeDelta), tileID);
		}
		
		/**
		 * Construct a new CornerFilter, accepting tiles with any altitude-delta and the
		 * given tile-ID.
		 * 
		 * @param tileID
		 *            {@code null} for no tile-ID filter
		 */
		public CornerFilter(String tileID) {
			
			this(OptionalInt.empty(), tileID);
		}
		
		public CornerFilter(OptionalInt altitudeDelta, String tileID) {
			
			this.altitudeDelta = altitudeDelta;
			this.tileID = tileID;
		}
		
		public OptionalInt getAltitudeDelta() {
			
			return altitudeDelta;
		}
		
		public String getTileID() {
			
			return tileID;
		}
	}
	
	public static enum Corner {
		
		NORTHWEST(-1, +1, 2),
		NORTHEAST(+1, +1, 3),
		SOUTHEAST(+1, -1, 0),
		SOUTHWEST(-1, -1, 1);
		
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
		
		private static final Corner[][] forDelta = new Corner[][] { { Corner.SOUTHWEST, Corner.NORTHWEST },
				{ Corner.SOUTHEAST, Corner.NORTHEAST } };
		
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
