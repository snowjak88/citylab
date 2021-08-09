/**
 * 
 */
package org.snowjak.city.map.tiles;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.tiles.support.TileSupport;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/**
 * Describes a single tile.
 * 
 * @author snowjak88
 *
 */
public class Tile implements Disposable {
	
	private final TileSet tilesetDescriptor;
	private final String id, filename;
	private final int x, y, gridWidth, gridHeight, padding, surfaceOffset, altitudeOffset;
	private final TileCorner base;
	private final boolean decoration;
	private final long hashcode;
	
	private final EnumMap<TileCorner, List<String>> provision = new EnumMap<>(TileCorner.class);
	private final List<TileRule<TileSupport>> rules = new LinkedList<>();
	
	private int width, height;
	private TextureRegion sprite;
	
	public Tile(TileSet tilesetDescriptor, String id, String filename, int x, int y, int width, int height,
			int gridWidth, int gridHeight, int padding, int surfaceOffset, int altitudeOffset, boolean decoration,
			TileCorner base, Map<TileCorner, List<String>> provision, List<TileRule<TileSupport>> rules) {
		
		this.tilesetDescriptor = tilesetDescriptor;
		this.id = id;
		this.filename = filename;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.padding = padding;
		this.surfaceOffset = surfaceOffset;
		this.altitudeOffset = altitudeOffset;
		this.decoration = decoration;
		this.base = base;
		
		this.provision.putAll(provision);
		this.rules.addAll(rules);
		
		this.hashcode = (id + filename + Integer.toString(this.x) + Integer.toString(this.y)
				+ Integer.toString(this.width) + Integer.toString(this.height) + Integer.toString(this.padding)
				+ Integer.toString(this.surfaceOffset) + Integer.toString(this.altitudeOffset)
				+ Boolean.toString(this.decoration)).hashCode();
	}
	
	public TileSet getTilesetDescriptor() {
		
		return tilesetDescriptor;
	}
	
	String getId() {
		
		return id;
	}
	
	public String getFilename() {
		
		return filename;
	}
	
	public long getHashcode() {
		
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
	
	public void setWidth(int width) {
		
		this.width = width;
	}
	
	public int getHeight() {
		
		return height;
	}
	
	public void setHeight(int height) {
		
		this.height = height;
	}
	
	public int getGridWidth() {
		
		return gridWidth;
	}
	
	public int getGridHeight() {
		
		return gridHeight;
	}
	
	public int getPadding() {
		
		return padding;
	}
	
	public int getSurfaceOffset() {
		
		return surfaceOffset;
	}
	
	public int getAltitudeOffset() {
		
		return altitudeOffset;
	}
	
	public boolean isDecoration() {
		
		return decoration;
	}
	
	public TileCorner getBase() {
		
		return base;
	}
	
	public EnumMap<TileCorner, List<String>> getProvision() {
		
		return provision;
	}
	
	/**
	 * Does this tile fit in the given map at the given cell-coordinates? (i.e., are
	 * all its configured rules satisfied?)
	 * 
	 * @param map
	 * @param cellX
	 * @param cellY
	 * @return
	 */
	public boolean isAcceptable(CityMap map, int cellX, int cellY) {
		
		for (TileRule<TileSupport> rule : rules)
			if (!rule.isAcceptable(map, cellX, cellY))
				return false;
		return true;
	}
	
	public TextureRegion getSprite() {
		
		return sprite;
	}
	
	public void setSprite(TextureRegion sprite) {
		
		this.sprite = sprite;
	}
	
	@Override
	public void dispose() {
		
		if (this.sprite != null)
			sprite.getTexture().dispose();
	}
}
