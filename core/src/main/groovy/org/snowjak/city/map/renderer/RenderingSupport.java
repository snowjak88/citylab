/**
 * 
 */
package org.snowjak.city.map.renderer;

import java.util.function.Consumer;

import org.snowjak.city.map.tiles.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

/**
 * @author snowjak88
 *
 */
public interface RenderingSupport {
	
	/**
	 * Render the given {@link Tile} at the given cell location.
	 * 
	 * @param cellX
	 * @param cellY
	 * @param tile
	 */
	public default void renderTile(int cellX, int cellY, Tile tile) {
		
		renderTile(cellX, cellY, tile, null);
	}
	
	/**
	 * Render the given {@link Tile} at the given cell location.
	 * 
	 * @param cellX
	 * @param cellY
	 * @param tile
	 * @param tint
	 *            {@code null} if no tint
	 */
	public void renderTile(int cellX, int cellY, Tile tile, Color tint);
	
	/**
	 * Execute a custom rendering method, that consumes the active {@link Batch} and
	 * uses it for some purpose.
	 * 
	 * @param customRenderer
	 */
	public void render(Consumer<Batch> customRenderer);
	
	/**
	 * Convert the given world-coordinates to viewport coordinates (i.e., what you'd
	 * need to perform drawing).
	 * 
	 * @param worldCoordinates
	 * @return
	 */
	public Vector2 worldToViewport(Vector2 worldCoordinates);
}
