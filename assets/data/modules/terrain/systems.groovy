
isCellMapper = ComponentMapper.getFor(IsMapCell)

//
// Any map-cell that doesn't already have a terrain-tile should get one!
//
iteratingSystem 'terrainFittingSystem', Family.all(IsMapCell).exclude(IsTerrainTile).get(), { entity, deltaTime ->
	def mapCell = isCellMapper.get(entity)
	
	def tiles = tileset.getMinimalTilesFor(state.map, (int) mapCell.cellX, (int) mapCell.cellY)
	
	def terrainTile = entity.addAndReturn(state.engine.createComponent(IsTerrainTile))
	terrainTile.tiles = tiles
}

//
// When a map-cell is "rearranged" -- i.e., it changes a corner-height, or flavor, or whatever --
// we need to make sure that we re-assign the terrain-tile.
//
listeningSystem 'terrainRearrangementSystem', Family.all(IsTerrainTile, IsMapCellRearranged).get(), { entity, deltaTime ->
	//
	// When we "hear" the IsMapCellRearranged hit our IsTerrainTile,
	// remove the IsTerrainTile
	//
	// If this entity is still a map-cell, then the 'terrainFittingSystem'
	// will take care of reassigning the terrain-tile
	//
	entity.remove IsTerrainTile
}, { entity, deltaTime ->
	//
	// nothing to do when the IsMapCellRearranged "drops off" this entity
	//
}