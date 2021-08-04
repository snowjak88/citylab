/**
 * 
 */
package org.snowjak.city.map.tiles;

/**
 * Describes the corners of a single tile.
 * 
 * @author snowjak88
 *
 */
public enum TileCorner {
	
	/**
	 * Top of tile-diamond.
	 */
	TOP(0, +1, -1, +1, 2),
	/**
	 * Right of tile-diamond.
	 */
	RIGHT(+1, +1, +1, +1, 3),
	/**
	 * Bottom of tile-diamond.
	 */
	BOTTOM(+1, 0, +1, -1, 0),
	/**
	 * Left of tile-diamond.
	 */
	LEFT(0, 0, -1, -1, 1);
	
	private final int offsetX, offsetY, dx, dy, oppositeIndex;
	
	private TileCorner(int offsetX, int offsetY, int dx, int dy, int oppositeIndex) {
		
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.dx = dx;
		this.dy = dy;
		this.oppositeIndex = oppositeIndex;
	}
	
	public int getOffsetX() {
		
		return offsetX;
	}
	
	public int getOffsetY() {
		
		return offsetY;
	}
	
	public int getDx() {
		
		return dx;
	}
	
	public int getDy() {
		
		return dy;
	}
	
	public TileCorner getOpposite() {
		
		return TileCorner.values()[oppositeIndex];
	}
}