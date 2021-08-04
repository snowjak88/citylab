/**
 * 
 */
package org.snowjak.city.map.tiles.support

import org.snowjak.city.map.tiles.Tile
import org.snowjak.city.map.tiles.TileCorner
import org.snowjak.city.map.tiles.TileRule
import org.snowjak.city.map.tiles.TileSet

/**
 * @author snowjak88
 *
 */
class TileSetDsl  {
	
	String title = "(untitled)"
	String description = "(no description)"
	
	int gridWidth = 32
	int gridHeight = 16
	
	int x = 0, y = 0
	int width = 32, height = 32
	int padding = 0, offset = 0
	String filename = ""
	boolean decoration = false
	TileCorner base = TileCorner.TOP
	
	private int autoAdvanceLimitX = 0, autoAdvanceLimitY = 0
	private boolean autoAdvance = false
	
	private List<TileDsl> tiles = []
	
	/**
	 * Build a new tile. Implicitly calls {@link #next()} after the tile is defined.
	 * @param script
	 */
	public void tile(@DelegatesTo(TileDsl) Closure script) {
		def tileDsl = [
			x: x, y: y,
			width: width, height: height,
			gridWidth: gridWidth, gridHeight: gridHeight,
			padding: padding, offset: offset,
			base: base,
			decoration: decoration,
			filename: filename
		] as TileDsl
		
		script.resolveStrategy = Closure.DELEGATE_FIRST
		script = script.rehydrate(tileDsl, this, this)
		//		script.delegate = tileDsl
		//		script.owner = this
		//		script.thisObject = this
		
		script()
		
		tiles << tileDsl
		
		next()
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
		def tsd = new TileSet(title, description, width, height, gridWidth, gridHeight, offset, padding)
		tiles.each { dsl ->
			def rules = dsl.rules.collect { r -> new TileRule(r, new TileRuleSupport()) }
			tsd.addTile new Tile(tsd, dsl.id, dsl.filename, dsl.x, dsl.y, dsl.width, dsl.height, dsl.gridWidth, dsl.gridHeight, dsl.padding, dsl.offset, dsl.decoration, dsl.base, dsl.provision, rules)
		}
		tsd
	}
}