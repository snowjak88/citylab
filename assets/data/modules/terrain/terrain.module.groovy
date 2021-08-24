id = 'terrain'
description = 'Handles fitting terrain-tiles to the map.'

//
// Get the configured tile-set name to use for the landscape,
// with a pre-programmed fallback
tilesetName = preference('tileset-name', 'default-landscape.tileset')

dependsOn TileSet, 'default'

//
// Get that tileset from the tile-set service.
//tileset = tileSetService.get(tilesetName)
tileset = assets.getByID 'default', TileSet

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
// This will be called every frame for every on-screen map-cell
//
cellRenderHook 0, { cellX, cellY, support ->
	for(def entity in data.map.getEntities(cellX, cellY, IsTerrainTile)) {
		for(def tile in terrainMapper.get(entity).tiles)
			support.renderTile cellX, cellY, tile
	}
}