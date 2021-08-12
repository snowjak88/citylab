/**
 * 
 */
package org.snowjak.city.map.tiles.support

import org.snowjak.city.map.tiles.MapMutator
import org.snowjak.city.map.tiles.Tile
import org.snowjak.city.map.tiles.TileCorner
import org.snowjak.city.map.tiles.TileRule
import org.snowjak.city.map.tiles.TileSet
import org.snowjak.city.util.validation.Validator
import org.snowjak.city.util.validation.Validator.ValidationException

/**
 * @author snowjak88
 *
 */
class TileSetDsl {
	
	String title = "(untitled)"
	String description = "(no description)"
	
	int gridWidth = 32
	int gridHeight = 16
	
	int x = 0, y = 0
	int width = 32, height = 32
	int padding = 0, surfaceOffset = 0, altitudeOffset = 0
	String folder = ""
	String filename = ""
	boolean decoration = false
	TileCorner base = TileCorner.TOP
	
	Map<String,Closure> ruleHelpers = [:]
	List<Closure> mutators = []
	
	private int autoAdvanceLimitX = 0, autoAdvanceLimitY = 0
	private boolean autoAdvance = false
	
	private List<TileDsl> tiles = []
	
	/**
	 * A {@link Validator} configured for TileSetDsl instances
	 */
	public static final Validator<TileSetDsl> VALIDATOR = Validator.getFor(TileSetDsl)
			.notBlank({it.title}, "Tile-set must have a non-blank title")
			.notEmpty({it.tiles}, "Tile-set doesn't define any tiles")
			.build()
	
	/**
	 * Applies {@link #VALIDATOR} to this TileSetDsl instance
	 * 
	 * @throws ValidationException
	 */
	public void validate() throws ValidationException {
		VALIDATOR.validate this
		
		for(def t in tiles)
			t.validate()
	}
	
	/**
	 * Build a new tile. Implicitly calls {@link #next()} after the tile is defined.
	 * @param script
	 */
	public void tile(@DelegatesTo(TileDsl) Closure script) {
		def tileDsl = [
			x: x, y: y,
			width: width, height: height,
			gridWidth: gridWidth, gridHeight: gridHeight,
			padding: padding, surfaceOffset: surfaceOffset,
			altitudeOffset: altitudeOffset,
			base: base,
			decoration: decoration,
			filename: filename,
			ruleHelpers: new HashMap(ruleHelpers)
		] as TileDsl
		
		script.resolveStrategy = Closure.DELEGATE_FIRST
		script = script.rehydrate(tileDsl, this, this)
		script()
		
		tileDsl.filename = (folder == null || folder.trim().isBlank()) ? tileDsl.filename : "$folder/${tileDsl.filename}"
		
		tiles << tileDsl
		
		next()
	}
	
	public void ruleHelper(String name, @DelegatesTo(TileSupport) Closure script) {
		ruleHelpers[name] = script
	}
	
	/**
	 * Register a new map-mutator.
	 * 
	 * @param script
	 */
	public void mutator(@DelegatesTo(MutatorTileSupport) Closure script) {
		
		mutators << script
	}
	
	/**
	 * Allow successive calls to {@link #next()} to "auto-advance" the
	 * default x,y coordinates by the default width/height values, within the given limits.
	 * @param autoAdvanceLimitX
	 * @param autoAdvanceLimitY
	 */
	public void autoAdvance(int autoAdvanceLimitX, int autoAdvanceLimitY) {
		
		if(autoAdvanceLimitX == 0 && autoAdvanceLimitY == 0) {
			this.autoAdvance = false
			return
		}
		
		this.autoAdvance = true
		this.autoAdvanceLimitX = autoAdvanceLimitX
		this.autoAdvanceLimitY = autoAdvanceLimitY
	}
	
	/**
	 * Advance the default x,y coordinates by the currently-configured width/height.
	 * <p>
	 * x is advanced first by width (either the tileset-configured width or the last-configured tile's width).
	 * If this takes x beyond the configured {@link #autoAdvance(int,int) auto-advance limit}, x is reset to 0
	 * and y is advanced by height.
	 * </p>
	 */
	public void next() {
		if(autoAdvance) {
			x += (tiles.isEmpty()) ? width : tiles.last().width
			if(x >= autoAdvanceLimitX)
				nextRow()
		}
	}
	
	/**
	 * Moves the tileset's x,y coordinates to the next row (i.e., add height to y and set x to 0).
	 */
	public void nextRow() {
		if(autoAdvance) {
			x = 0
			y += (tiles.isEmpty()) ? height : tiles.last().height
			
			if(y >= autoAdvanceLimitY)
				y = 0
		}
	}
	
	TileSet build() {
		
		def mutatorInstances = new LinkedList<MapMutator>()
		mutators.each { m -> mutatorInstances << new MapMutator(m, new HashMap(ruleHelpers), new MutatorTileSupport()) }
		
		def tsd = new TileSet(title, description, width, height, gridWidth, gridHeight, surfaceOffset, altitudeOffset, padding, mutatorInstances)
		
		tiles.each { dsl ->
			
			def rules = new LinkedList<>()
			dsl.rules.each { r -> rules << new TileRule(r, dsl.ruleHelpers, new TileSupport()) }
			def tile = new Tile(tsd, dsl.id, dsl.filename, dsl.x, dsl.y, dsl.width, dsl.height, dsl.gridWidth, dsl.gridHeight, dsl.padding, dsl.surfaceOffset, dsl.altitudeOffset, dsl.decoration, dsl.base, dsl.provision, rules)
			for(TileCorner c : TileCorner.values())
				tile.provision.computeIfAbsent c, { k -> new LinkedList<>() }
			
			tsd.addTile tile
		}
		tsd
	}
}