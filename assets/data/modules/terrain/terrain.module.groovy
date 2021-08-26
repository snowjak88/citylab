import java.lang.Math

id = 'terrain'
description = 'Handles fitting terrain-tiles to the map.'

//
// Get the configured tile-set name to use for the landscape,
// with a pre-programmed fallback
tilesetName = preferences().getString('tileset-name', 'default')

//
// Get that tileset from the tile-set service.
// Note that we need to mark that tile-set as explicitly depended-upon.
//
dependsOn tilesetName, TileSet

tileset = assets.getByID tilesetName, TileSet

//
// This module's systems need this Component-class.
//
class IsTerrainTile implements Component {
	
	List<Tile> tiles = []
}

//
// ComponentMappers make us faster at querying and retrieving Components from entities
terrainMapper = ComponentMapper.getFor(IsTerrainTile)
atCellMapper = ComponentMapper.getFor(AtMapCell)

//
// This module declares its entity-processing systems in another file.
// That file is loaded and processed now.
//
include 'systems.groovy'

//
// Declare a cell-rendering hook into the map-rendering loop.
// This will be called every frame for every on-screen map-cell.
//
// Note that this hook as a name.
// Names must be unique for cell-rendering hooks. Hooks that are
// registered later will overwrite those registered earlier.
//
cellRenderHook 'terrainRender', { delta, cellX, cellY, support ->
	for(def entity in data.map.getEntities(cellX, cellY, IsTerrainTile)) {
		for(def tile in terrainMapper.get(entity).tiles)
			support.renderTile cellX, cellY, tile
	}
}

//
// Declare a "custom" rendering hook into the map-rendering loop.
// This is called only once per frame.
//
// As with cell-rendering hooks, custom-rendering hooks have IDs, too, which also
// are susceptible of being overwritten by other custom-rendering hooks.
//
// Note how we prioritize this renderer, relative to the map-renderer (which has the ID "map"),
// which executes all those cell-render-hooks.
//
// You indicate priorities using both "before" and "after", and you can prioritize both
// custom- and cell-rendering hooks.
//
dependsOn 'cloud.png', Texture
cloudTexture = assets.get( 'cloud.png', Texture )

cloudOffsetX = 0f
cloudOffsetY = 0f

customRenderHook ('clouds', { delta, batch, shapeDrawer, support ->
	def viewBounds = support.viewportWorldBounds
	
	final float cloudWidth = 4
	final float cloudHeight = cloudWidth * (cloudTexture.height / cloudTexture.width)
	
	final cloudSpacingX = cloudWidth * 13
	final cloudSpacingY = cloudHeight * 7
	
	float originX = cloudOffsetX + delta
	float originY = cloudOffsetY + delta
	
	cloudOffsetX = Util.wrap( cloudOffsetX + delta, 0, cloudSpacingX )
	cloudOffsetY = Util.wrap( cloudOffsetY - delta, 0, cloudSpacingY )
	
	def startX = Util.wrap( originX, viewBounds.x - cloudSpacingX, viewBounds.x )
	def startY = Util.wrap( originY, viewBounds.y - cloudSpacingY, viewBounds.y )
	def endX = startX + viewBounds.width + cloudSpacingX
	def endY = startY + viewBounds.height + cloudSpacingY
	
	batch.color = Color.WHITE
	for(def x=startX; x<=endX; x += cloudSpacingX)
		for(def y=startY; y<=endY; y += cloudSpacingY)
			batch.draw cloudTexture, (float) x, (float) y, (float) cloudWidth, (float) cloudHeight
	
}).after('map')
