/**
 * 
 */
package org.snowjak.city.map

import org.snowjak.city.GameData
import org.snowjak.city.map.tiles.Tile
import org.snowjak.city.map.tiles.TileCorner
import org.snowjak.city.map.tiles.TileSet

/**
 * Manages the game map.
 * <p>
 * A CityMap is basically an array of vertices. Each vertex is associated with a
 * list of flavors -- say, {@code GRASS}, {@code ROAD}, {@code CANAL}, etc. Tiles span
 * the quadrilaterals between each set of 4 vertices, and are selected for their
 * ability to accommodate these characteristics.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class CityMap {
	
	private List<String>[][] vertices
	private int[][] vertexAltitudes
	private List<Tile>[][] cells
	
	/**
	 * Are the given cell-coordinates located within this map?
	 * @param cellX
	 * @param cellY
	 * @return
	 */
	public boolean isValidCell(int cellX, int cellY) {
		( (cells != null)
				&& (cellX >= 0) && (cellY >= 0)
				&& (cellX < cells.length) && ( cellY < cells[cellX].length) )
	}
	
	/**
	 * Construct a new map of the given dimensions in tiles. The number of vertices will be +1 in each direction.
	 * 
	 * @param width
	 * @param height
	 */
	public CityMap(int width, int height) {
		
		vertices = new List<String>[width+1][height+1]
		vertexAltitudes = new int[width+1][height+1]
		cells = new List<Tile>[width][height]
	}
	
	/**
	 * For the tile located at ( {@code cellX}, {@code cellY} ), return the list of
	 * flavors associated with the given {@link TileCorner corner}.
	 * <p>
	 * Equivalent to {@link #getVertexFlavors(int, int) getVertexFlavors(cellX +
	 * corner.getOffsetX(), cellY + corner.getOffsetY())}.
	 * </p>
	 * 
	 * @param cellX
	 * @param cellY
	 * @param corner
	 * @return
	 * @throws NullPointerException
	 *             if {@code corner} is {@code null}
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code cellX + corner.getOffsetX()}) or
	 *             ({@code cellY + corner.getOffsetY()}) fall outside the map
	 */
	public List<String> getTileCornerFlavors(int cellX, int cellY, TileCorner corner)
	throws NullPointerException, ArrayIndexOutOfBoundsException {
		
		if (corner == null)
			throw new NullPointerException("Cannot get corner flavors for null corner.")
		
		getVertexFlavors(cellX + corner.offsetX, cellY + corner.offsetY)
	}
	
	/**
	 * Return list of flavors associated with the vertex located at (
	 * {@code vertexX}, {@code vertexY} ).
	 * 
	 * @param vertexX
	 * @param vertexY
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code vertexX}) or ({@code vertexY}) fall outside the map
	 */
	public List<String> getVertexFlavors(int vertexX, int vertexY) {
		
		if (vertexX < 0 || vertexY < 0 || vertexX >= vertices.length || vertexY >= vertices[vertexY].length)
			throw new ArrayIndexOutOfBoundsException(
			String.format("Given vertex index [%d,%d] is out of bounds.", vertexX, vertexY))
		
		if (vertices[vertexX][vertexY] == null)
			return Collections.emptyList()
		
		Collections.unmodifiableList(vertices[vertexX][vertexY])
	}
	
	/**
	 * Get the altitude associated with the given cell at the given corner.
	 * <p>
	 * Equivalent to {@link #getVertexAltitude(int, int) getVertexAltitude(cellX +
	 * corner.getOffsetX(), cellY + corner.getOffsetY())}.
	 * </p>
	 * 
	 * @param vertexX
	 * @param vertexY
	 * @return
	 * @throws NullPointerException
	 *             if {@code corner} is {@code null}
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code cellX + corner.getOffsetX()}) or
	 *             ({@code cellY + corner.getOffsetY()}) fall outside the map
	 */
	public int getTileAltitude(int cellX, int cellY, TileCorner corner) {
		
		if (corner == null)
			throw new NullPointerException("Cannot get corner altitude for null corner.")
		
		getVertexAltitude(cellX + corner.offsetX, cellY + corner.offsetY)
	}
	
	/**
	 * Get the altitude associated with the given vertex.
	 * 
	 * @param vertexX
	 * @param vertexY
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code vertexX}) or ({@code vertexY}) fall outside the map
	 */
	public int getVertexAltitude(int vertexX, int vertexY) {
		
		if (vertexX < 0 || vertexY < 0 || vertexX >= vertexAltitudes.length
				|| vertexY >= vertexAltitudes[vertexY].length)
			throw new ArrayIndexOutOfBoundsException(
			String.format("Given vertex index [%d,%d] is out of bounds.", vertexX, vertexY))
		
		vertexAltitudes[vertexX][vertexY]
	}
	
	/**
	 * Set the altitude associated with the given vertex.
	 * <p>
	 * Note that this method doesn't change any {@link Tile} assignments by itself.
	 * </p>
	 * 
	 * @param vertexX
	 * @param vertexY
	 * @param altitude
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code vertexX}) or ({@code vertexY}) fall outside the map
	 */
	public void setVertexAltitude(int vertexX, int vertexY, int altitude) {
		
		if (vertexX < 0 || vertexY < 0 || vertexX >= vertexAltitudes.length
				|| vertexY >= vertexAltitudes[vertexY].length)
			throw new ArrayIndexOutOfBoundsException(
			String.format("Given vertex index [%d,%d] is out of bounds.", vertexX, vertexY))
		
		vertexAltitudes[vertexX][vertexY] = altitude
	}
	
	/**
	 * Sets the flavors associated with the given vertex.
	 * <p>
	 * Note that this method doesn't change any {@link Tile} assignments by itself.
	 * </p>
	 * 
	 * @param vertexX
	 * @param vertexY
	 * @param flavors
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code vertexX}) or ({@code vertexY}) fall outside the map
	 */
	public void setVertexFlavors(int vertexX, int vertexY, List<String> flavors) {
		
		if (vertexX < 0 || vertexY < 0 || vertexX >= vertexAltitudes.length
				|| vertexY >= vertexAltitudes[vertexY].length)
			throw new ArrayIndexOutOfBoundsException(
			String.format("Given vertex index [%d,%d] is out of bounds.", vertexX, vertexY))
		
		vertices[vertexX][vertexY] = flavors
	}
	
	/**
	 * Get the list of assigned {@link Tile}s at this location, or {@link Collections#emptyList()} if no Tiles are assigned.
	 * @param cellX
	 * @param cellY
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code cellX}) or
	 *             ({@code cellY}) fall outside the map
	 */
	public List<Tile> getTiles(int cellX, int cellY) {
		if(!isValidCell(cellX, cellY))
			throw new ArrayIndexOutOfBoundsException(String.format("Given cell index [%d,%d] is out of bounds.", cellX, cellY));
		
		if( cells[cellX][cellY] == null )
			return Collections.emptyList()
		
		Collections.unmodifiableList cells[cellX][cellY]
	}
	
	/**
	 * Calls {@link #updateTiles(TileSet)} with the currently-registered {@link TileSet} in {@link GameData}.
	 * <p>
	 * If {@link GameData#tileset} is not set, does nothing.
	 * </p>
	 */
	public void updateTiles() {
		updateTiles GameData.get().tileset
	}
	
	/**
	 * Updates the {@link Tile} assignments for every square in this map.
	 * @param tilset
	 */
	public void updateTiles(TileSet tilset) {
		updateTiles tilset, 0, 0, cells.length, cells[0].length
	}
	
	/**
	 * Updates the {@link Tile} assignments for every square in this map, starting
	 * from cell {@code startX,startY} and proceeding in a rectangle of {@code width,height} cells.
	 * 
	 * @param tileset
	 * @param startX
	 * @param startY
	 * @param width
	 * @param height
	 */
	public void updateTiles(TileSet tileset, int startX, int startY, int width, int height) {
		for(int x in startX..startX+width-1) {
			if(x < 0 || x >= cells.length)
				continue;
			
			for(int y in startY..startY+height-1) {
				if(y < 0 || y >= cells[x].length)
					continue;
				
				cells[x][y] = tileset.getMinimalTilesFor(this, x, y)
			}
		}
	}
}