/**
 * 
 */
package org.snowjak.city.map.tiles
import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
import static org.snowjak.city.util.Util.*

import java.util.function.Consumer
import java.util.function.Predicate

import org.codehaus.groovy.util.HashCodeHelper
import org.snowjak.city.map.tiles.support.TileSupport
import org.snowjak.city.resources.ScriptedResource
import org.snowjak.city.util.validation.Validator
import org.snowjak.city.util.validation.Validator.ValidationException

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
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
	boolean decoration = false
	TileCorner base = TileCorner.TOP
	
	boolean redrawFront = false, redrawBack = false
	
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
			redrawFront: redrawFront, redrawBack: redrawBack,
			decoration: decoration,
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
			decoration: decoration,
			ruleHelpers: ruleHelpers, ext: ext
		] as TileSet
		configurer.accept tileset
		
		tileset.folder = tileset.scriptDirectory
		
		script.run()
		
		this.tiles.addAll tileset.tiles
		
		tileset
	}
	
	private static final CACHE_SIZE = 1024
	private final transient LinkedHashMap<Integer,Tile> __combinedTiles = new LinkedHashMap<>(CACHE_SIZE, 0.75, true)
	
	/**
	 * Construct a new Tile using the {@link #getMinimalTilesFor(int[][], EnumMap, List, boolean) minimal tile-set}
	 * needed to meet the given constraints.
	 * <p>
	 * Because this method needs to create a new {@link Texture}, you must ensure this is called from the main (OpenGL) thread!
	 * </p>
	 * <p>
	 * <strong>Note</strong> that, because this Tile's associated Texture is not managed by the application's AssetManager,
	 * you the caller should ensure that this Tile is {@link Disposable#dispose() disposed of} when no longer needed --
	 * probably by adding it to {@link GameState#disposables}.
	 * </p>
	 * <p>
	 * If desired, you can supply a mapping from TileCorners to Colors. Each tile will use the Color (if any) corresponding to its {@link Tile#base}.
	 * </p>
	 * 
	 * @param heights
	 * @return
	 */
	@Deprecated
	public Tile getCombinedTileFor(List<Tile> tiles, int[][] heights, EnumMap<TileCorner,Color> tints = new EnumMap(TileCorner)) {
		
		//
		// "Normalize" the height-values so we can draw these tiles with their lowest corner(s)
		// at altitude = 0.
		def altitudeNormalization = Integer.MAX_VALUE
		def altitudeNormalizationCorner = null
		for(def x in 0..1)
			for(def y in 0..1)
				if(heights[x][y] < altitudeNormalization) {
					altitudeNormalization = heights[x][y]
					altitudeNormalizationCorner = TileCorner.fromOffset(x, y)
				}
		
		final int[][] normalizedHeights = new int[2][2]
		for(def x in 0..1)
			for(def y in 0..1)
				normalizedHeights[x][y] = heights[x][y] - altitudeNormalization
		
		def hash = HashCodeHelper.initHash()
		hash = HashCodeHelper.updateHash(hash, tiles.hashCode())
		hash = HashCodeHelper.updateHash(hash, normalizedHeights)
		hash = HashCodeHelper.updateHash(hash, tints)
		
		synchronized(__combinedTiles) {
			if(__combinedTiles.containsKey(hash))
				return __combinedTiles[hash]
		}
		
		//
		// Compute the maximum extents required by the found Tiles
		def maxGridWidth = 0f
		def maxGridHeight = 0f
		def maxWidth = 0f
		def maxHeight = 0f
		def maxSurfaceOffset = 0f
		def maxAltitudeOffset = 0f
		def isDecoration = true
		
		final provision = new EnumMap<TileCorner,Set<String>>(TileCorner)
		final rules = new LinkedHashSet<TileRule<TileSupport>>()
		tiles.each { t ->
			
			if(maxGridWidth == 0 && maxGridHeight == 0) {
				maxGridWidth = t.gridWidth
				maxGridHeight = t.gridHeight
				maxWidth = t.width
				maxHeight = t.height
				maxSurfaceOffset = t.surfaceOffset
				maxAltitudeOffset = t.altitudeOffset
				
			} else {
				
				if(t.gridWidth > maxGridWidth)
					maxWidth *= t.gridWidth / maxGridWidth
				
				if(t.gridHeight > maxGridHeight) {
					final heightScale = t.gridHeight / maxGridHeight
					maxHeight *= heightScale
					maxSurfaceOffset *= heightScale
					maxAltitudeOffset *= heightScale
				}
				
				maxGridWidth = max( maxGridWidth, t.gridWidth)
				maxGridHeight = max( maxGridHeight, t.gridHeight )
				maxWidth = max( maxWidth, t.width )
				maxHeight = max( maxHeight, t.height )
				maxSurfaceOffset = max( maxSurfaceOffset, t.surfaceOffset )
				maxAltitudeOffset = max( maxAltitudeOffset, t.altitudeOffset )
				
				isDecoration = isDecoration && t.decoration
			}
			
			t.provision.each { c, p ->
				provision.computeIfAbsent(c, { _ -> new LinkedHashSet<>() }).addAll p
			}
			
			rules.addAll t.rules
		}
		
		//
		// Allocate a buffer onto which we can successively draw these tiles
		final buffer = new Pixmap((int) maxWidth, (int) maxHeight, RGBA8888)
		
		//
		// Draw these tiles onto the buffer.
		tiles.each { t ->
			final heightScale = t.gridHeight / maxGridHeight
			
			final int drawWidth = maxWidth
			final int drawHeight = t.height * heightScale
			
			final altitude = heights[t.base.offsetX][t.base.offsetY] - altitudeNormalization
			final altitudeOffset = t.altitudeOffset * heightScale * altitude
			
			final surfaceOffset = t.surfaceOffset * heightScale
			
			final int drawX = 0
			final int drawY = height - altitudeOffset - maxSurfaceOffset + surfaceOffset
			
			final textureData = t.sprite.texture.textureData
			if(!textureData.isPrepared())
				textureData.prepare()
			
			buffer.color = Color.WHITE
			if(tints)
				if(tints[t.base])
					buffer.color = tints[t.base]
			
			buffer.drawPixmap textureData.consumePixmap(), t.sprite.regionX, t.sprite.regionY, t.sprite.regionWidth, t.sprite.regionHeight, drawX, drawY, drawWidth, drawHeight
		}
		
		final combinedTile = new Tile(
				gridWidth: maxGridWidth, gridHeight: maxGridHeight,
				width: maxWidth, height: maxHeight,
				decoration: decoration,
				base: altitudeNormalizationCorner,
				altitudeOffset: maxAltitudeOffset,
				surfaceOffset: maxSurfaceOffset,
				sprite: new TextureRegion(new Texture(buffer)))
		provision.each { corner, flavors -> combinedTile.provision[corner] = flavors }
		combinedTile.rules.addAll rules
		
		synchronized(__combinedTiles) {
			final int toRemove = max( 0, __combinedTiles.size() - CACHE_SIZE )
			final combinedIterator = __combinedTiles.iterator()
			for(def i=0; i<toRemove && combinedIterator.hasNext(); i++) {
				combinedIterator.next()
				combinedIterator.remove()
			}
			
			__combinedTiles[hash] = combinedTile
		}
		combinedTile
	}
	
	/**
	 * Get the minimum set of Tiles that can fit the given constraints:
	 * <ul>
	 * <li>{@code heights} -- a 2x2 {@code int} array (addressed using {@link TileCorner#offsetX},{@link TileCorner#offsetY})</li>
	 * <li>{@code predicates} -- a list of {@link Predicate}s that the returned set of Tiles must collectively satisfy</li>
	 * </ul>
	 */
	public List<Tile> getMinimalTilesFor(int[][] heights, List<Predicate<Tile>> predicates, boolean onlyDecorative = false) {
		
		return searchMinimalTilesFor(heights, predicates, new HashSet<Tile>(), onlyDecorative)
	}
	
	private List<Tile> searchMinimalTilesFor(int[][] heights,
			List<Predicate<Tile>> remainingPredicates, Set<Tile> currentTiles, boolean onlyDecorative, int depth = 0) {
		
		if (remainingPredicates.isEmpty() && !currentTiles.isEmpty())
			return Collections.emptyList()
		
		List<Tile> bestTileList = null
		List<Tile> currentTileList = new LinkedList<>()
		
		//
		// Consider each tile in order.
		//
		for (Tile tile : tiles) {
			
			//
			// If the tile is non-transparent and we're not allowing that -- skip it.
			if (onlyDecorative && !tile.isDecoration())
				continue
			
			//
			// If we've already added this tile to the current chain -- skip it.
			if (currentTiles.contains(tile))
				continue
			
			//
			// The "new remaining" list of predicates is the old list, minus the
			// currently-selected tile's fulfilled predicates
			final List<Predicate<Tile>> newRemaining = []
			remainingPredicates.findAll { !(it as Predicate).test(tile) }.each { newRemaining << it }
			
			//
			// If the current tile doesn't fulfill *any* predicates, skip it
			if (newRemaining.size() == remainingPredicates.size())
				continue
			
			//
			// If the tile's rules don't allow it to fit here -- skip it.
			if (!tile.isAcceptable(heights))
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
			final List<Tile> searchResult = searchMinimalTilesFor(heights, newRemaining, currentTiles, true, depth + 1)
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
