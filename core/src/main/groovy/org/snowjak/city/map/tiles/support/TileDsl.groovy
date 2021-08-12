/**
 * 
 */
package org.snowjak.city.map.tiles.support

import org.snowjak.city.map.tiles.TileCorner
import org.snowjak.city.util.validation.Validator
import org.snowjak.city.util.validation.Validator.ValidationException

/**
 * @author snowjak88
 *
 */
class TileDsl {
	private TileSetDsl tsd
	
	String id
	int x, y
	int width, height
	int gridWidth, gridHeight
	int padding, surfaceOffset, altitudeOffset
	String filename
	TileCorner base
	
	boolean decoration = false
	
	Map<TileCorner,List<String>> provision = [:]
	List<Closure> rules = []
	Map<String,Closure> ruleHelpers = [:]
	
	/**
	 * A {@link Validator} for TileDsl instances.
	 */
	public static final Validator<TileDsl> VALIDATOR = Validator.getFor(TileDsl.class)
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
				new LinkedList<>(provision)
			})
		}
	}
	
	/**
	 * Define a rule for this tile. For a tile to be considered for placement on the map, every one of its defined rules must be met.
	 * @param rule
	 */
	public void rule(@DelegatesTo(TileSupport) Closure rule) {
		rules << rule
	}
}