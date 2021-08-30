/**
 * 
 */
package org.snowjak.city.map.tiles

import org.snowjak.city.map.CityMap
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
	
	FileHandle folder
	String filename
	
	int x, y, width, height, padding
	int gridWidth, gridHeight
	int surfaceOffset, altitudeOffset
	TileCorner base = TileCorner.TOP
	
	boolean decoration = false
	
	Map<String,Closure> ruleHelpers = new LinkedHashMap<>()
	
	TextureRegion sprite
	
	final Map<TileCorner,Set<String>> provision = new EnumMap<>(TileCorner)
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
				.andAny()
					.notEmpty({it.provision[TileCorner.TOP]})
					.notEmpty({it.provision[TileCorner.RIGHT]})
					.notEmpty({it.provision[TileCorner.LEFT]})
					.notEmpty({it.provision[TileCorner.BOTTOM]})
				.endAny("Tile must define at least one 'provides'.")
				.build()
	
	/**
	 * Applies {@link #VALIDATOR} to this TileDsl
	 * @throws ValidationException
	 */
	public void validate() throws ValidationException {
		VALIDATOR.validate this
	}
	
	/**
	 * Denotes that this tile provides the given flavors to all corners.
	 * <p>
	 * Note that successive {@code provides()} calls are additive upon one another.
	 * </p>
	 * @param provision
	 */
	public void provides(String... provision) {
		provides([
			TileCorner.TOP,
			TileCorner.RIGHT,
			TileCorner.BOTTOM,
			TileCorner.LEFT
		], provision.toList())
	}
	
	/**
	 * Denotes that this tile provides the given flavors to the following corners.
	 * <p>
	 * Note that successive {@code provides()} calls are additive upon one another.
	 * </p>
	 * @param corners
	 * @param provision
	 */
	public void provides(List<TileCorner> corners, List<String> provision) {
		corners.each { corner ->
			this.provision.computeIfAbsent(corner, { c ->
				new LinkedHashSet<>(provision)
			})
		}
	}
	
	/**
	 * Define a rule for this tile. For a tile to be considered for placement on the map, every one of its defined rules must be met.
	 * @param rule
	 */
	public void rule(@DelegatesTo(TileSupport) Closure rule) {
		this.rules << new TileRule(rule, ruleHelpers, new TileSupport())
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
		
		//
		// A tile doesn't fit if any of its rules fail.
		//
		return !rules.any { !it.isAcceptable(map,cellX, cellY) }
	}
	
	
	
	@Override
	public int hashCode() {
		
		final int prime = 31
		int result = 1
		result = prime * result + altitudeOffset
		result = prime * result + ((base == null) ? 0 : base.hashCode())
		result = prime * result + (decoration ? 1231 : 1237)
		result = prime * result + ((filename == null) ? 0 : filename.hashCode())
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