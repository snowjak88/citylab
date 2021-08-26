
//
// Declare an entity-processing system.
//

isCellMapper = ComponentMapper.getFor(IsMapCell)

iteratingSystem 'terrainFittingSystem', Family.all(IsMapCell).exclude(IsTerrainTile).get(), { entity, deltaTime ->
	def mapCell = isCellMapper.get(entity)
	
	def tiles = tileset.getMinimalTilesFor(data.map, (int) mapCell.cellX, (int) mapCell.cellY)
	
	def terrainTile = entity.addAndReturn(data.engine.createComponent(IsTerrainTile))
	terrainTile.tiles = tiles
}