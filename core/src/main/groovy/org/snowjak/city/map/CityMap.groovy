/**
 * 
 */
package org.snowjak.city.map

import org.snowjak.city.map.tiles.TileCorner

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity

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
	private Entity[][] entities
	
	private final Map<Class<? extends Component>,ComponentMapper> componentMappers = [:]
	
	/**
	 * Are the given cell-coordinates located within this map?
	 * @param cellX
	 * @param cellY
	 * @return
	 */
	public boolean isValidCell(int cellX, int cellY) {
		( (entities != null)
				&& (cellX >= 0) && (cellY >= 0)
				&& (cellX < getWidth()) && ( cellY < getHeight()) )
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
		entities = new Entity[width][height]
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
	 * Set the altitude associated with the given corner of the given cell. (Delegates to
	 * {@link #setVertexAltitude(int, int, int) setVertexAltitude(cellX + corner.offsetX, cellY + corner.offsetY)}.)
	 * @param cellX
	 * @param cellY
	 * @param corner
	 * @param altitude
	 */
	public void setTileCornerAltitude(int cellX, int cellY, TileCorner corner, int altitude) {
		
		if (corner == null)
			throw new NullPointerException("Cannot set corner altitude for null corner.")
		
		setVertexAltitude cellY + corner.offsetX, cellY + corner.offsetY, altitude
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
	 * Sets the flavors associated with the given corner of the given cell.
	 * @param cellX
	 * @param cellY
	 * @param corner
	 * @param flavors
	 */
	public void setTileCornerFlavors(int cellX, int cellY, TileCorner corner, List<String> flavors) {
		
		if (corner == null)
			throw new NullPointerException("Cannot set corner flavors for null corner.")
		
		setVertexFlavors cellX + corner.offsetX, cellY + corner.offsetY, flavors
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
		
		if(vertices[vertexX][vertexY] == null)
			vertices[vertexX][vertexY] = new ArrayList<>()
		else
			vertices[vertexX][vertexY].clear()
		
		vertices[vertexX][vertexY].addAll flavors
	}
	
	/**
	 * Get the associated {@link Entity} at this location, or null if no Entity is associated.
	 * @param cellX
	 * @param cellY
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code cellX}) or
	 *             ({@code cellY}) fall outside the map
	 */
	public Entity getEntity(int cellX, int cellY) {
		
		if(!isValidCell(cellX, cellY))
			throw new ArrayIndexOutOfBoundsException(String.format("Given cell index [%d,%d] is out of bounds.", cellX, cellY))
		
		return entities[cellX][cellY]
	}
	
	/**
	 * Associate the given Entity to the given cell. Overwrites any previous association.
	 * 
	 * <p>
	 * <strong>Note</strong> that this doesn't check to see if this Entity
	 * is already associated with any other cell -- you have to take care of that bookkeeping yourself.
	 * </p>
	 * 
	 * @param cellX
	 * @param cellY
	 * @param entity
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code cellX}) or
	 *             ({@code cellY}) fall outside the map
	 */
	public void setEntity( int cellX, int cellY, Entity entity) {
		
		entities[cellX][cellY] = entity
	}
	
	/**
	 * Returns the width of this map, expressed in cells. This map's width in vertices will be this value, plus 1.
	 * @return
	 */
	public int getWidth() {
		entities.length
	}
	
	/**
	 * Returns the height of this map, expressed in cells. This map's height in vertices will be this value, plus 1.
	 * @return
	 */
	public int getHeight() {
		entities[0].length
	}
}