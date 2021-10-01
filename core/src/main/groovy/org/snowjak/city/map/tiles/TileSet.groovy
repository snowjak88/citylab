/**
 * 
 */
package org.snowjak.city.map.tiles
import java.util.function.Consumer

import org.snowjak.city.GameState
import org.snowjak.city.map.tiles.support.TileSupport
import org.snowjak.city.resources.ScriptedResource
import org.snowjak.city.util.validation.Validator
import org.snowjak.city.util.validation.Validator.ValidationException

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Disposable

/**
 * @author snowjak88
 *
 */
class TileSet extends ScriptedResource implements Disposable {
	
	String title = "(untitled)"
	String description = "(no description)"
	
	int gridWidth = 32
	int gridHeight = 16
	
	int x, y
	int width = 32, height = 32
	int padding = 0, surfaceOffset = 0, altitudeOffset = 0
	FileHandle folder
	FileHandle atlas
	String filename = ""
	int zOrder = 0
	boolean transparent = false
	TileCorner base = TileCorner.TOP
	
	Map<String,Closure> ruleHelpers = [:]
	
	Expando ext = new Expando()
	private int autoAdvanceLimitX = 0, autoAdvanceLimitY = 0
	private boolean autoAdvance = false
	
	Set<Tile> tiles = []
	
	/**
	 * A {@link Validator} configured for TileSetDsl instances
	 */
	public static final Validator<TileSet> VALIDATOR = Validator.getFor(TileSet)
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
		tiles.each { it.validate() }
	}
	
	//
	// You can set the [folder] property either with a FileHandle, or with a String
	//
	public void setFolder(FileHandle folder) {
		this.folder = folder
	}
	
	public void setFolder(String folder) {
		this.folder = file(folder)
	}
	
	public void setAtlas(FileHandle atlas) {
		this.atlas = atlas
	}
	
	public void setAtlas(String atlas) {
		this.atlas = file(atlas)
	}
	
	/**
	 * Add a new tile definition. Implicitly calls {@link #next()} after the tile is defined.
	 * @param script
	 */
	public void tile(@DelegatesTo(Tile) Closure script) {
		def tile = [
			folder: folder, filename: filename, atlas: atlas,
			x: x, y: y,
			width: width, height: height, padding: padding,
			gridWidth: gridWidth, gridHeight: gridHeight,
			surfaceOffset: surfaceOffset, altitudeOffset: altitudeOffset,
			base: base,
			zOrder: zOrder, transparent: transparent,
			ruleHelpers: new HashMap(ruleHelpers)
		] as Tile
		
		tile.ext.properties.putAll this.ext.properties
		
		script.resolveStrategy = Closure.DELEGATE_FIRST
		script = script.rehydrate(tile, this, this)
		script()
		
		tile.folder = tile.folder ?: this.folder ?: this.scriptDirectory
		
		tiles << tile
		
		if(tile.atlas) {
			addAssetDependency TextureAtlas, tile.atlas
		} else {
			def tileFile = tile.folder.child(tile.filename)
			addAssetDependency Texture, tileFile
		}
		
		next()
	}
	
	/**
	 * Register a new rule-helper: a user-defined function that can be leveraged by tile-rules.
	 * 
	 * @param name
	 * @param script
	 */
	public void ruleHelper(String name, @DelegatesTo(TileSupport) Closure script) {
		ruleHelpers[name] = script
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
	
	@Override
	protected ScriptedResource executeInclude(FileHandle includeHandle, Consumer<ScriptedResource> configurer,
			DelegatingScript script) {
		
		final tileset = [
			atlas: atlas,
			width: width, height: height, padding: padding,
			gridWidth: gridWidth, gridHeight: gridHeight,
			surfaceOffset: surfaceOffset, altitudeOffset: altitudeOffset,
			base: base,
			transparent: transparent, zOrder: zOrder,
			ruleHelpers: ruleHelpers, ext: ext
		] as TileSet
		configurer.accept tileset
		
		tileset.folder = tileset.scriptDirectory
		
		script.run()
		
		this.tiles.addAll tileset.tiles
		
		tileset
	}
	
	/**
	 * Get the Tile that can best fit the given constraints:
	 * <ul>
	 * <li>{@code heights} -- a 2x2 {@code int} array (addressed using {@link TileCorner#offsetX},{@link TileCorner#offsetY})</li>
	 * <li>{@code ext} -- a set of properties that the returned Tile must match</li>
	 * </ul>
	 */
	public Tile getTileFor(int[][] heights, Expando ext) {
		
		final result = []
		
		//
		// Consider each tile in order.
		//
		for (Tile tile : tiles) {
			
			//
			// If the tile's rules don't allow it to fit here -- skip it.
			if (!tile.isAcceptable(heights, ext))
				continue
			
			result << tile
		}
		
		if(result.isEmpty())
			return null
		
		result[GameState.RND.nextInt(result.size())]
	}
	
	@Override
	public void dispose() {
		
		tiles.each { it.dispose() }
	}
}
