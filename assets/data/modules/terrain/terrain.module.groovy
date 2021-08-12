id = 'terrain'
description = 'Handles fitting terrain-tiles to the map.'

//
// This module's systems need this Component-class.
//
class HasTerrainTile implements Component {
	
	List<Tile> tiles = []
}

//
// Get the configured tile-set name to use for the landscape,
// with a pre-programmed fallback
tilesetName = preference('tileset-name', 'default-landscape.tileset')
//
// Get that tileset from the tile-set service.
tileset = tileSetService.get(tilesetName)

//
// ComponentMappers make us faster at querying and retrieving Components from entities
terrainMapper = ComponentMapper.getFor(HasTerrainTile)
atCellMapper = ComponentMapper.getFor(AtMapCell)

//
// Declare an entity-processing system.
//
iteratingSystem 'terrainFittingSystem', Family.all(AtMapCell).exclude(HasTerrainTile).get(), { entity, deltaTime ->
	def mapCell = atCellMapper.get(entity)
	
	def tiles = tileset.getMinimalTilesFor(data.map, (int) mapCell.cellX, (int) mapCell.cellY)
	
	def terrainTile = entity.addAndReturn(new HasTerrainTile())
	terrainTile.tiles = tiles
}

//
// Declare a hook into the map-rendering loop.
//
renderHook 0, { cellX, cellY, support ->
	for(def entity in data.map.getEntities(cellX, cellY, HasTerrainTile))
		for(def tile in terrainMapper.get(entity).tiles)
			support.renderTile cellX, cellY, tile
}