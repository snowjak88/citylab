/**
 * 
 */
package org.snowjak.city.map;

import java.util.EnumMap;

/**
 * @author snowjak88
 *
 */
public abstract class Map {
	
	private EnumMap<MapDomain, TileSet> tilesets = new EnumMap<>(MapDomain.class);
	
	public TileSet getTileSetFor(MapDomain domain) {
		
		return tilesets.get(domain);
	}
	
	public void setTileSetFor(MapDomain domain, TileSet tileset) {
		
		tilesets.put(domain, tileset);
	}
	
	public abstract boolean isLocationInBounds(int x, int y);
	
	/**
	 * Assign the given value to the given dimension at the given location.
	 * 
	 * @param x
	 * @param y
	 * @param dimension
	 * @param value
	 * @throws IndexOutOfBoundsException
	 *             if the given location is not within the map's bounds
	 */
	public abstract void setCell(int x, int y, String dimension, int value);
	
	/**
	 * Assign the given value to the given dimension at the given location.
	 * 
	 * @param x
	 * @param y
	 * @param dimension
	 * @param value
	 * @throws IndexOutOfBoundsException
	 *             if the given location is not within the map's bounds
	 */
	public abstract void setCell(int x, int y, String dimension, float value);
	
	/**
	 * Assign the given value to the given dimension at the given location.
	 * 
	 * @param x
	 * @param y
	 * @param dimension
	 * @param value
	 * @throws IndexOutOfBoundsException
	 *             if the given location is not within the map's bounds
	 */
	public abstract void setCell(int x, int y, String dimension, String value);
	
	/**
	 * Assign the given value to the given dimension at the given location.
	 * 
	 * @param x
	 * @param y
	 * @param dimension
	 * @param value
	 * @param type
	 *            used for error-checking when retrieving values
	 * @throws IndexOutOfBoundsException
	 *             if the given location is not within the map's bounds
	 */
	public abstract <T> void setCell(int x, int y, String dimension, T value, Class<T> type);
	
	/**
	 * Get the int-value associated with the given dimension at the given location.
	 * 
	 * @param x
	 * @param y
	 * @param dimension
	 * @return {@code 0} if no value is associated with the given dimension at this
	 *         location
	 * @throws IndexOutOfBoundsException
	 *             if the given location is not within the map's bounds
	 */
	public abstract int getCellInt(int x, int y, String dimension);
	
	/**
	 * Get the float-value associated with the given dimension at the given
	 * location.
	 * 
	 * @param x
	 * @param y
	 * @param dimension
	 * @return {@code 0.0} if no value is associated with the given dimension at
	 *         this location
	 * @throws IndexOutOfBoundsException
	 *             if the given location is not within the map's bounds
	 */
	public abstract float getCellFloat(int x, int y, String dimension);
	
	/**
	 * Get the String-value associated with the given dimension at the given
	 * location.
	 * 
	 * @param x
	 * @param y
	 * @param dimension
	 * @return {@code null} if no value is associated with the given dimension at
	 *         this location
	 * @throws IndexOutOfBoundsException
	 *             if the given location is not within the map's bounds
	 */
	public abstract String getCellString(int x, int y, String dimension);
	
	/**
	 * Get the object associated with the given dimension at the given location.
	 * 
	 * @param x
	 * @param y
	 * @param dimension
	 * @param type
	 * @return {@code null} if no value is associated with the given dimension at
	 *         this location
	 * @throws IndexOutOfBoundsException
	 *             if the given location is not within the map's bounds
	 * @throws ClassCastException
	 *             if the stored value cannot be cast to the desired type
	 */
	public abstract <T> T getCellObject(int x, int y, String dimension, Class<T> type);
}
