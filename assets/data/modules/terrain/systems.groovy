
//
// Declare an entity-processing system.
//
iteratingSystem 'terrainFittingSystem', Family.all(AtMapCell).exclude(IsTerrainTile).get(), { entity, deltaTime ->
	def mapCell = atCellMapper.get(entity)
	
	def tiles = tileset.getMinimalTilesFor(data.map, (int) mapCell.cellX, (int) mapCell.cellY)
	
	def terrainTile = entity.addAndReturn(new IsTerrainTile())
	terrainTile.tiles = tiles
}