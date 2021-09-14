/**
 * 
 */
package org.snowjak.city.map.tiles

import org.snowjak.city.map.tiles.support.TileSupport
import org.snowjak.city.util.validation.Validator
import org.snowjak.city.util.validation.Validator.ValidationException

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Disposable

/**
 * @author snowjak88
 *
 */
class Tile implements Disposable {
	
	String id
	
	FileHandle atlas
	FileHandle folder
	String filename
	
	int x, y, width, height, padding
	int gridWidth, gridHeight
	int surfaceOffset, altitudeOffset
	TileCorner base = TileCorner.TOP
	
	boolean decoration = false
	
	Map<String,Closure> ruleHelpers = new LinkedHashMap<>()
	final Expando ext = new Expando()
	
	TextureRegion sprite
	
	final Set<TileRule<TileSupport>> rules = new HashSet<>()
	
	/**
	 * A {@link Validator} for Tile instances.
	 */
	public static final Validator<Tile> VALIDATOR = Validator.getFor(Tile.class)
	.notBlank({ it.id }, "Tile ID must not be blank")
	.greaterThan({ it.gridWidth }, 0, "Tile's grid-width must be a positive number")
	.greaterThan({ it.gridHeight }, 0, "Tile's grid-height must be a positive number")
	.notBlank({it.filename}, "Tile must reference an image-file")
	.notNull({it.base},"Tile's base must not be null")
	.build()
	
	/**
	 * Applies {@link #VALIDATOR} to this TileDsl
	 * @throws ValidationException
	 */
	public void validate() throws ValidationException {
		VALIDATOR.validate this
	}
	
	def propertyMissing(name) {
		ext.getProperty(name)
	}
	
	def propertyMissing(name, value) {
		ext.setProperty name, value
	}
	
	/**
	 * Define a rule for this tile. For a tile to be considered for placement on the map, every one of its defined rules must be met.
	 * @param rule
	 */
	public void rule(@DelegatesTo(TileSupport) Closure rule) {
		this.rules << new TileRule(rule, ruleHelpers, new TileSupport())
	}
	
	/**
	 * Does this tile fit, given the specified local attributes:
	 * <ul>
	 * <li>{@code localHeight} -- an {@code int[2][2]} holding this tile's heights (addressed via {@link TileCorner#offsetX},{@link TileCorner#offsetY})</li>
	 * </ul>
	 *
	 * @param localHeight
	 * @param localFlavors
	 * @return
	 */
	public boolean isAcceptable(int[][] localHeight) {
		
		//
		// A tile doesn't fit if any of its rules fail.
		//
		return !rules.any { !it.isAcceptable(localHeight) }
	}
	
	
	
	@Override
	public int hashCode() {
		
		final int prime = 31
		int result = 1
		result = prime * result + altitudeOffset
		result = prime * result + ((base == null) ? 0 : base.hashCode())
		result = prime * result + (decoration ? 1231 : 1237)
		result = prime * result + ((filename == null) ? 0 : filename.hashCode())
		result = prime * result + ((atlas == null) ? 0 : atlas.hashCode())
		result = prime * result + ((folder == null) ? 0 : folder.path().hashCode())
		result = prime * result + gridHeight
		result = prime * result + gridWidth
		result = prime * result + height
		result = prime * result + ((id == null) ? 0 : id.hashCode())
		result = prime * result + padding
		result = prime * result + surfaceOffset
		result = prime * result + width
		result = prime * result + x
		result = prime * result + y
		result
	}
	
	@Override
	public void dispose() {
		
		sprite?.texture?.dispose()
	}
}
