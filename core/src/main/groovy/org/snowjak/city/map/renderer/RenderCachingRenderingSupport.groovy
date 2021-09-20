package org.snowjak.city.map.renderer

import org.snowjak.city.map.tiles.Tile
import org.snowjak.city.map.tiles.TileCorner

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.Pool.Poolable

class RenderCachingRenderingSupport implements RenderingSupport {
	final MapRenderer renderer
	final LinkedList<RenderBean> cached = new LinkedList()
	
	public RenderCachingRenderingSupport(MapRenderer renderer) {
		this.renderer = renderer
	}
	
	@Override
	public Rectangle getViewportWorldBounds() {
		
		renderer.viewportWorldBounds
	}
	
	@Override
	public void renderTile(int cellX, int cellY, Tile tile, int altitudeOverride) {
		
		final rb = Pools.obtain(RenderBean)
		rb.cellX = cellX
		rb.cellY = cellY
		rb.tile = tile
		rb.altitudeOverride = altitudeOverride
		cached << rb
	}
	
	@Override
	public boolean isPointVisible(int viewportX, int viewportY) {
		
		renderer.isPointVisible viewportX, viewportY
	}
	
	@Override
	public boolean isPointVisible(float viewportX, float viewportY) {
		
		renderer.isPointVisible viewportX, viewportY
	}
	
	@Override
	public boolean isCellVisible(int cellX, int cellY) {
		
		renderer.isCellVisible cellX, cellY
	}
	
	@Override
	public Vector2 mapToViewport(Vector2 mapCoordinates) {
		
		renderer.mapToViewport mapCoordinates
	}
	
	@Override
	public Vector2[] getCellVertices(int cellX, int cellY, TileCorner base) {
		
		renderer.getCellVertices cellX, cellY, base
	}
	
	@Override
	public Vector2 getVertex(int vertexX, int vertexY) {
		
		renderer.getVertex vertexX, vertexY
	}
	
	public static class RenderBean implements Poolable {
		int cellX, cellY, altitudeOverride
		Tile tile
		
		void reset() {
			cellX = 0
			cellY = 0
			altitudeOverride = 0
			tile = null
		}
	}
}
