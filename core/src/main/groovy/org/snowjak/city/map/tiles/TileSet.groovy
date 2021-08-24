/**
 * 
 */
package org.snowjak.city.map.tiles

import java.util.function.Consumer
import org.snowjak.city.map.CityMap
import org.snowjak.city.map.tiles.support.TileSupport
import org.snowjak.city.resources.ScriptedResource
import org.snowjak.city.util.validation.Validator
import org.snowjak.city.util.validation.Validator.ValidationException

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
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
	String filename = ""
	boolean decoration = false
	TileCorner base = TileCorner.TOP
	
	Map<String,Closure> ruleHelpers = [:]
	
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
	
	/**
	 * Add a new tile definition. Implicitly calls {@link #next()} after the tile is defined.
	 * @param script
	 */
	public void tile(@DelegatesTo(Tile) Closure script) {
		def tile = [
			folder: folder, filename: filename,
			x: x, y: y,
			width: width, height: height, padding: padding,
			gridWidth: gridWidth, gridHeight: gridHeight,
			surfaceOffset: surfaceOffset, altitudeOffset: altitudeOffset,
			base: base,
			decoration: decoration,
			ruleHelpers: new HashMap(ruleHelpers)
		] as Tile
		
		script.resolveStrategy = Closure.DELEGATE_FIRST
		script = script.rehydrate(tile, this, this)
		script()
		
		tile.folder = tile.folder ?: this.folder ?: this.scriptDirectory
		
		tiles << tile
		
		def tileFile = tile.folder.child(tile.filename)
		addAssetDependency tileFile.path(), Texture
		
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
			width: width, height: height, padding: padding,
			gridWidth: gridWidth, gridHeight: gridHeight,
			surfaceOffset: surfaceOffset, altitudeOffset: altitudeOffset,
			base: base,
			decoration: decoration,
			ruleHelpers: ruleHelpers
			] as TileSet
		configurer.accept tileset
		
		script.run()
		
		this.tiles.addAll tileset.tiles
		
		tileset
	}
	
	/**
	 * Get the minimum set of Tiles that can fit the given map at the given
	 * location.
	 * <p>
	 * You should probably execute {@link #mutate(CityMap, int, int) mutate()}
	 * against this location before attempting to look for fitting tiles.
	 * </p>
	 * <p>
	 * Note that this should only look at vertex-altitudes and -flavors (barring any
	 * special Tile rules, which would be executed normally).
	 * </p>
	 */
	public List<Tile> getMinimalTilesFor(CityMap map, int cellX, int cellY) {
		
		final EnumMap<TileCorner, List<String>> remainingFlavors = new EnumMap<>(TileCorner)
		for (TileCorner corner : TileCorner.values())
			remainingFlavors[corner] = new LinkedList<String>(map.getTileCornerFlavors(cellX, cellY, corner))
		
		return searchMinimalTilesFor(map, cellX, cellY, remainingFlavors, new HashSet<Tile>(), true)
	}
	
	private List<Tile> searchMinimalTilesFor(CityMap map, int cellX, int cellY,
			EnumMap<TileCorner, List<String>> remainingFlavors, Set<Tile> currentTiles, boolean nonDecorative) {
		
		boolean allDone = true
		for (TileCorner corner : remainingFlavors.keySet()) {
			if (!remainingFlavors.get(corner).isEmpty()) {
				allDone = false
				break
			}
		}
		
		if (allDone)
			return Collections.emptyList()
		
		List<Tile> bestTileList = null
		List<Tile> currentTileList = new LinkedList<>()
		
		//
		// Consider each tile in order.
		//
		for (Tile tile : tiles) {
			
			//
			// If the tile is non-transparent and we're not allowing that -- skip it.
			if (nonDecorative == tile.isDecoration())
				continue
				
			//
			// If we've already added this tile to the current chain -- skip it.
			if (currentTiles.contains(tile))
				continue
				
			//
			// The tile must not add anything we don't already need
			boolean addsExtra = false
			for (TileCorner corner : remainingFlavors.keySet()) {
				if (addsExtra)
					break
				for (String flavor : tile.getProvision().get(corner))
					if (!remainingFlavors.get(corner).contains(flavor)) {
						addsExtra = true
						break
					}
			}
			if (addsExtra)
				continue
				
			//
			// The "new remaining" list of flavors is the old list, minus the
			// currently-selected tile's flavors
			boolean fulfillsAnyFlavors = false
			final EnumMap<TileCorner, List<String>> newRemaining = new EnumMap<>(TileCorner)
			for (TileCorner corner : remainingFlavors.keySet()) {
				final List<String> remaining = new LinkedList<String>(remainingFlavors.get(corner))
				fulfillsAnyFlavors = fulfillsAnyFlavors || remaining.removeAll(tile.getProvision().get(corner))
				newRemaining[corner] = remaining
			}
			if (!fulfillsAnyFlavors)
				continue
				
			//
			// If the tile's rules don't allow it to fit here -- skip it.
			if (!tile.isAcceptable(map, cellX, cellY))
				continue
				
			//
			// OK -- now get ready to go a level deeper.
			//
			// Prepare the list that will receive the results of our search.
			//
			if (currentTileList == null)
				currentTileList = new LinkedList<>()
			else
				currentTileList.clear()
			currentTileList << tile
			
			//
			// We don't want to select the current tile again in our search.
			currentTiles << tile
			
			//
			// Do the search and capture the results in the list.
			final List<Tile> searchResult = searchMinimalTilesFor(map, cellX, cellY, newRemaining, currentTiles, false)
			if (searchResult != null)
				currentTileList.addAll searchResult
			
			currentTiles.remove tile
			
			//
			// If we haven't chosen a best list yet,
			// or if the current list is shorter than our best,
			// make the current our best.
			if (bestTileList == null || bestTileList.size() > currentTileList.size()) {
				bestTileList = currentTileList
				currentTileList = null
			}
		}
		
		bestTileList
	}
	
	@Override
	public void dispose() {
		
		tiles.each { it.dispose() }
	}
}
