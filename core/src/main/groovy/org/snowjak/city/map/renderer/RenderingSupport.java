/**
 * 
 */
package org.snowjak.city.map.renderer;

import org.snowjak.city.map.tiles.Tile;
import org.snowjak.city.map.tiles.TileCorner;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * @author snowjak88
 *
 */
public interface RenderingSupport {
	
	/**
	 * Get the {@link Rectangle} corresponding to the current viewport, in World
	 * coordinates.
	 * 
	 * @return
	 */
	public Rectangle getViewportWorldBounds();
	
	/**
	 * Schedule the given {@link Tile} to be rendered at the given cell location.
	 * 
	 * @param cellX
	 * @param cellY
	 * @param tile
	 */
	public default void renderTile(int cellX, int cellY, Tile tile) {
		
		renderTile(cellX, cellY, tile, null);
	}
	
	/**
	 * Schedule the given {@link Tile} to be rendered at the given cell location.
	 * 
	 * @param cellX
	 * @param cellY
	 * @param tile
	 * @param tint
	 *            {@code null} if no tint
	 */
	public void renderTile(int cellX, int cellY, Tile tile, Color tint);
	
	/**
	 * 
	 * @param viewportX
	 * @param viewportY
	 * @return {@code true} if the given point (given in viewport coordinates) is
	 *         currently visible
	 */
	public boolean isPointVisible(int viewportX, int viewportY);
	
	/**
	 * 
	 * @param viewportX
	 * @param viewportY
	 * @return {@code true} if the given point (given in viewport coordinates) is
	 *         currently visible
	 */
	public boolean isPointVisible(float viewportX, float viewportY);
	
	/**
	 * 
	 * @param cellX
	 * @param cellY
	 * @return {@code true} if the given cell is currently visible
	 */
	public boolean isCellVisible(int cellX, int cellY);
	
	/**
	 * Convert the given map-coordinates to viewport coordinates (i.e., what you'd
	 * need to perform drawing).
	 * 
	 * @param mapCoordinates
	 * @return
	 */
	public Vector2 mapToViewport(Vector2 mapCoordinates);
	
	/**
	 * Given a map-cell (identified with {@code x,y}), compute its 4 vertices in
	 * terms of the viewport's coordinate system.
	 * <p>
	 * Vertices are given clockwise from (-x,-y):
	 * 
	 * <pre>
	 * x1,y1
	 * x1,y2
	 * x2,y2
	 * x2,y1
	 * </pre>
	 * </p>
	 * <p>
	 * <strong>Note</strong> that, to save on garbage-collection, the Vector2[]
	 * instance that's returned should be considered to be owned by this
	 * RenderingSupport instance. Do not plan on the value of this array remaining
	 * stable over time.
	 * </p>
	 * 
	 * @param cellX
	 * @param cellY
	 * @param base
	 *            use TileCorner as the basis for altitude calculations (to produce
	 *            a "flat" set of vertices), or {@code null} to use altitude at each
	 *            vertex
	 * @return
	 */
	public Vector2[] getCellVertices(int cellX, int cellY, TileCorner base);
}
