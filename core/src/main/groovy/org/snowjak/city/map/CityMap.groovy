/**
 * 
 */
package org.snowjak.city.map

import org.snowjak.city.map.tiles.TileCorner
import org.snowjak.city.util.BiIntConsumer

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
	private Entity[][] cellEntities, vertexEntities
	
	private final Collection<BiIntConsumer> vertexChangedListeners = Collections.synchronizedCollection( [] )
	
	/**
	 * Construct a new map of the given dimensions in tiles. The number of vertices will be +1 in each direction.
	 * 
	 * @param width
	 * @param height
	 */
	public CityMap(int width, int height) {
		
		vertices = new List<String>[width+1][height+1]
		vertexAltitudes = new int[width+1][height+1]
		cellEntities = new Entity[width][height]
		vertexEntities = new Entity[width+1][height+1]
	}
	
	/**
	 * Add a listener (a {@link BiIntConsumer}) that will be notified whenever a vertex on this map changes in any way.
	 * @param listener
	 */
	public void addVertexChangedListener(BiIntConsumer listener) {
		vertexChangedListeners << listener
	}
	
	/**
	 * Are the given cell-coordinates located within this map?
	 * @param cellX
	 * @param cellY
	 * @return
	 */
	public boolean isValidCell(int cellX, int cellY) {
		( (cellX >= 0) && (cellY >= 0)
				&& (cellX < getWidth()) && ( cellY < getHeight()) )
	}
	
	/**
	 * Are the given vertex-coordinates valid?
	 * @param vertexX
	 * @param vertexY
	 * @return
	 */
	public boolean isValidVertex(int vertexX, int vertexY) {
		( (vertexX >= 0) && (vertexY >= 0)
				&& (vertexX < vertices.length ) && (vertexY < vertices[vertexX].length))
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
	public int getCellAltitude(int cellX, int cellY, TileCorner corner) {
		
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
	public void setCellAltitude(int cellX, int cellY, TileCorner corner, int altitude) {
		
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
		vertexChangedListeners.each { it.accept(vertexX, vertexY) }
	}
	
	/**
	 * Sets the flavors associated with the given corner of the given cell.
	 * @param cellX
	 * @param cellY
	 * @param corner
	 * @param flavors
	 */
	public void setCellFlavors(int cellX, int cellY, TileCorner corner, List<String> flavors) {
		
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
			vertices[vertexX][vertexY] = new LinkedList<>()
		else
			vertices[vertexX][vertexY].clear()
		
		vertices[vertexX][vertexY].addAll flavors
		vertexChangedListeners.each { it.accept(vertexX, vertexY) }
	}
	
	/**
	 * Adds the given flavors to the list associated with the given vertex.
	 * @param vertexX
	 * @param vertexY
	 * @param flavors
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code vertexX}) or ({@code vertexY}) fall outside the map
	 */
	public void addVertexFlavors(int vertexX, int vertexY, List<String> flavors) {
		
		if (vertexX < 0 || vertexY < 0 || vertexX >= vertexAltitudes.length
				|| vertexY >= vertexAltitudes[vertexY].length)
			throw new ArrayIndexOutOfBoundsException(
			String.format("Given vertex index [%d,%d] is out of bounds.", vertexX, vertexY))
		
		flavors.each { flavor -> addInternal vertexX, vertexY, flavor }
	}
	
	/**
	 * Adds the given flavor to the list associated with the given vertex.
	 * @param vertexX
	 * @param vertexY
	 * @param flavor
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code vertexX}) or ({@code vertexY}) fall outside the map
	 */
	public void addVertexFlavor(int vertexX, int vertexY, String flavor) {
		
		if (vertexX < 0 || vertexY < 0 || vertexX >= vertexAltitudes.length
				|| vertexY >= vertexAltitudes[vertexY].length)
			throw new ArrayIndexOutOfBoundsException(
			String.format("Given vertex index [%d,%d] is out of bounds.", vertexX, vertexY))
		
		addInternal vertexX, vertexY, flavor
	}
	
	private void addInternal(int vertexX, int vertexY, String flavor) {
		if(vertices[vertexX][vertexY] == null)
			vertices[vertexX][vertexY] = new LinkedList<>()
		
		if(!vertices[vertexX][vertexY].contains(flavor))
			vertices[vertexX][vertexY] << flavor
	}
	
	/**
	 * Removes the given flavors from the list associated with the given vertex.
	 * @param vertexX
	 * @param vertexY
	 * @param flavors
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code vertexX}) or ({@code vertexY}) fall outside the map
	 */
	public void removeVertexFlavors(int vertexX, int vertexY, List<String> flavors) {
		
		if (vertexX < 0 || vertexY < 0 || vertexX >= vertexAltitudes.length
				|| vertexY >= vertexAltitudes[vertexY].length)
			throw new ArrayIndexOutOfBoundsException(
			String.format("Given vertex index [%d,%d] is out of bounds.", vertexX, vertexY))
		
		flavors.each { flavor -> removeInternal vertexX, vertexY, flavor }
	}
	
	/**
	 * Removes the given flavor from the list associated with the given vertex.
	 * @param vertexX
	 * @param vertexY
	 * @param flavor
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code vertexX}) or ({@code vertexY}) fall outside the map
	 */
	public void removeVertexFlavor(int vertexX, int vertexY, String flavor) {
		
		if (vertexX < 0 || vertexY < 0 || vertexX >= vertexAltitudes.length
				|| vertexY >= vertexAltitudes[vertexY].length)
			throw new ArrayIndexOutOfBoundsException(
			String.format("Given vertex index [%d,%d] is out of bounds.", vertexX, vertexY))
		
		removeInternal vertexX, vertexY, flavor
	}
	
	private void removeInternal(int vertexX, int vertexY, String flavor) {
		if(vertices[vertexX][vertexY] == null)
			return
		
		vertices[vertexX][vertexY].remove flavor
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
		
		return cellEntities[cellX][cellY]
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
		
		if(!isValidCell(cellX, cellY))
			throw new ArrayIndexOutOfBoundsException(String.format("Given cell index [%d,%d] is out of bounds.", cellX, cellY))
		
		cellEntities[cellX][cellY] = entity
	}
	
	/**
	 * Get the {@link Entity} associated with the given vertex, or null if no Entity is associated.
	 * @param vertexX
	 * @param vertexY
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code vertexX}) or
	 *             ({@code vertexY}) fall outside the map
	 */
	public Entity getVertexEntity(int vertexX, int vertexY) {
		
		if(!isValidVertex(vertexX, vertexY))
			throw new ArrayIndexOutOfBoundsException(
			String.format("Given vertex index [%d,%d] is out of bounds.", vertexX, vertexY))
		
		return vertexEntities[vertexX][vertexY]
	}
	
	/**
	 * Associate the given Entity to the given vertex. Overwrites any previous association.
	 *
	 * <p>
	 * <strong>Note</strong> that this doesn't check to see if this Entity
	 * is already associated with any other vertex -- you have to take care of that bookkeeping yourself.
	 * </p>
	 *
	 * @param vertexX
	 * @param vertexY
	 * @param entity
	 * @throws ArrayIndexOutOfBoundsException
	 *             if ({@code vertexX}) or
	 *             ({@code vertexY}) fall outside the map
	 */
	public void setVertexEntity( int vertexX, int vertexY, Entity entity) {
		
		if(!isValidVertex(vertexX, vertexY))
			throw new ArrayIndexOutOfBoundsException(
			String.format("Given vertex index [%d,%d] is out of bounds.", vertexX, vertexY))
		
		vertexEntities[vertexX][vertexY] = entity
	}
	
	/**
	 * Returns the width of this map, expressed in cells. This map's width in vertices will be this value, plus 1.
	 * @return
	 */
	public int getWidth() {
		cellEntities.length
	}
	
	/**
	 * Returns the height of this map, expressed in cells. This map's height in vertices will be this value, plus 1.
	 * @return
	 */
	public int getHeight() {
		cellEntities[0].length
	}
}