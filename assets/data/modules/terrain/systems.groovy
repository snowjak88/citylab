
isCellMapper = ComponentMapper.getFor(IsMapCell)
isTerrainTile = ComponentMapper.getFor(IsTerrainTile)

//
// Any map-cell that doesn't already have a terrain-tile should get one!
// Any map-cell that needs a replement
//
iteratingSystem 'newTerrainFittingSystem', Family.all(IsMapCell).exclude(IsTerrainTile).get(), { entity, deltaTime ->
	final mapCell = isCellMapper.get(entity)
	
	//
	// Note that we use the Engine to create a new IsTerrainTile component
	// This lets us take advantage of the Engine's built-in Component-pooling functionality,
	// reducing the amount of garbage-collection we have to do.
	//
	final terrainTile = entity.addAndReturn(state.engine.createComponent(IsTerrainTile))
	terrainTile.tiles = tileset.getMinimalTilesFor(state.map, (int) mapCell.cellX, (int) mapCell.cellY)
}

iteratingSystem 'existingTerrainUpdatingSystem', Family.all(IsMapCell, IsTerrainTile, NeedsReplacementTerrainTile).get(), { entity, deltaTime ->
	
	final mapCell = isCellMapper.get(entity)
	final terrainTile = isTerrainTile.get(entity)
	
	newTiles = tileset.getMinimalTilesFor(state.map, (int) mapCell.cellX, (int) mapCell.cellY)
	terrainTile.tiles = newTiles
	
	entity.remove NeedsReplacementTerrainTile
}

//
// When a map-cell is "rearranged" -- i.e., it changes a corner-height, or flavor, or whatever --
// we need to make sure that we re-assign the terrain-tile.
//
listeningSystem 'terrainRearrangementSystem', Family.all(IsTerrainTile, IsMapCellRearranged).get(), { entity, deltaTime ->
	//
	// When we "hear" the IsMapCellRearranged hit our IsTerrainTile,
	// flag the entity so we can regenerate its terrain.
	//
	// If this entity is still a map-cell, then the 'terrainFittingSystem'
	// will take care of reassigning the terrain-tile
	//
	entity.add state.engine.createComponent(NeedsReplacementTerrainTile)
}, { entity, deltaTime ->
	//
	// nothing to do when the IsMapCellRearranged "drops off" this entity
	//
}