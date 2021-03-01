/**
 * 
 */
package org.snowjak.city.map;

import java.util.HashMap;

/**
 * A map that is bounded -- has limits, beyond which it is not defined.
 * 
 * @author snowjak88
 *
 */
public class BoundedMap extends Map {
	
	private final int width, height;
	
	private final java.util.Map<String, Integer>[][] intValues;
	private final java.util.Map<String, Float>[][] floatValues;
	private final java.util.Map<String, String>[][] stringValues;
	private final java.util.Map<String, Class<?>>[][] objectTypes;
	private final java.util.Map<String, Object>[][] objectValues;
	
	@SuppressWarnings("unchecked")
	public BoundedMap(int width, int height) {
		
		this.width = width;
		this.height = height;
		
		intValues = new HashMap[width][height];
		floatValues = new HashMap[width][height];
		stringValues = new HashMap[width][height];
		objectTypes = new HashMap[width][height];
		objectValues = new HashMap[width][height];
		
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				intValues[x][y] = new HashMap<>();
				floatValues[x][y] = new HashMap<>();
				stringValues[x][y] = new HashMap<>();
				objectTypes[x][y] = new HashMap<>();
				objectValues[x][y] = new HashMap<>();
			}
	}
	
	@Override
	public boolean isLocationInBounds(int x, int y) {
		
		if (x < 0 || y < 0)
			return false;
		if (x >= width || y >= height)
			return false;
		return true;
	}
	
	@Override
	public void setCell(int x, int y, String dimension, int value) {
		
		if (!isLocationInBounds(x, y))
			throw new IndexOutOfBoundsException();
		intValues[x][y].put(dimension, Integer.valueOf(value));
	}
	
	@Override
	public void setCell(int x, int y, String dimension, float value) {
		
		if (!isLocationInBounds(x, y))
			throw new IndexOutOfBoundsException();
		floatValues[x][y].put(dimension, Float.valueOf(value));
	}
	
	@Override
	public void setCell(int x, int y, String dimension, String value) {
		
		if (!isLocationInBounds(x, y))
			throw new IndexOutOfBoundsException();
		stringValues[x][y].put(dimension, value);
	}
	
	@Override
	public <T> void setCell(int x, int y, String dimension, T value, Class<T> type) {
		
		if (!isLocationInBounds(x, y))
			throw new IndexOutOfBoundsException();
		objectTypes[x][y].put(dimension, type);
		objectValues[x][y].put(dimension, value);
	}
	
	@Override
	public int getCellInt(int x, int y, String dimension) {
		
		if (!isLocationInBounds(x, y))
			throw new IndexOutOfBoundsException();
		final Integer value = intValues[x][y].get(dimension);
		if (value == null)
			return 0;
		return value.intValue();
	}
	
	@Override
	public float getCellFloat(int x, int y, String dimension) {
		
		if (!isLocationInBounds(x, y))
			throw new IndexOutOfBoundsException();
		final Float value = floatValues[x][y].get(dimension);
		if (value == null)
			return 0f;
		return value.floatValue();
	}
	
	@Override
	public String getCellString(int x, int y, String dimension) {
		
		if (!isLocationInBounds(x, y))
			throw new IndexOutOfBoundsException();
		return stringValues[x][y].get(dimension);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCellObject(int x, int y, String dimension, Class<T> type) {
		
		if (!isLocationInBounds(x, y))
			throw new IndexOutOfBoundsException();
		
		final Class<?> retrievedType = objectTypes[x][y].get(dimension);
		
		if (!type.isAssignableFrom(retrievedType))
			throw new ClassCastException();
		
		return (T) objectTypes[x][y].get(dimension);
	}
}
