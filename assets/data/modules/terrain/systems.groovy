
isCellMapper = ComponentMapper.getFor(IsMapCell)
terrainTileMapper = ComponentMapper.getFor(IsTerrainTile)
pendingTerrainTileMapper = ComponentMapper.getFor(PendingTerrainTile)

//
// Any map-cell that doesn't already have a terrain-tile should get one!
//
iteratingSystem 'newTerrainFittingSystem', Family.all(IsMapCell).exclude(IsTerrainTile, PendingTerrainTile).get(), { entity, deltaTime ->
	final mapCell = isCellMapper.get(entity)
	final int cellX = mapCell.cellX
	final int cellY = mapCell.cellY
	
	final terrainHeights = new int[2][2]
	final terrainFlavors = new EnumMap(TileCorner)
	for(def corner : TileCorner.values()) {
		terrainHeights[corner.offsetX][corner.offsetY] = state.map.getCellAltitude(cellX, cellY, corner)
		terrainFlavors.put corner, ['grass']
	}
	
	final pendingTerrain = state.engine.createComponent(PendingTerrainTile)
	pendingTerrain.future = submitResultTask {
		->
		tileset.getMinimalTilesFor terrainHeights, terrainFlavors
	}
	entity.add pendingTerrain
	
}

iteratingSystem 'existingTerrainUpdatingSystem', Family.all(IsMapCell, NeedsReplacementTerrainTile).exclude(PendingTerrainTile).get(), { entity, deltaTime ->
	
	final mapCell = isCellMapper.get(entity)
	final int cellX = mapCell.cellX
	final int cellY = mapCell.cellY
	
	final pendingTerrain = state.engine.createComponent(PendingTerrainTile)
	
	final terrainHeights = new int[2][2]
	final terrainFlavors = new EnumMap(TileCorner)
	for(def corner : TileCorner.values()) {
		terrainHeights[corner.offsetX][corner.offsetY] = state.map.getCellAltitude(cellX, cellY, corner)
		terrainFlavors.put corner, ['grass']
	}
	
	pendingTerrain.future = submitResultTask {
		->
		tileset.getMinimalTilesFor terrainHeights, terrainFlavors
	}
	entity.add pendingTerrain
	
	entity.remove NeedsReplacementTerrainTile
}

iteratingSystem 'pendingTerrainUpdatingSystem', Family.all(PendingTerrainTile).get(), { entity, deltaTime ->
	
	final pendingTerrain = pendingTerrainTileMapper.get(entity)
	if(!pendingTerrain.future.isDone())
		return
	
	//
	// We always want to remove the PendingTerrainTile component (at the end of this system),
	// but we need not always create an IsTerrainTile component.
	//
	if(isCellMapper.has(entity)) {
		
		final isMapCell = isCellMapper.get(entity)
		
		IsTerrainTile terrainTile = null
		if(terrainTileMapper.has(entity))
			terrainTile = terrainTileMapper.get(entity)
		else
			terrainTile = entity.addAndReturn(state.engine.createComponent(IsTerrainTile))
		
		terrainTile.tiles = pendingTerrain.future.get() ?: []
	}
	
	entity.remove PendingTerrainTile
}

//
// When a map-cell is "rearranged" -- i.e., it changes a corner-height, or flavor, or whatever --
// we need to make sure that we re-assign the terrain-tile.
//
listeningSystem 'terrainRearrangementSystem', Family.all(IsTerrainTile, IsMapCellRearranged).exclude(NeedsReplacementTerrainTile).get(), { entity, deltaTime ->
	//
	// When we "hear" the IsMapCellRearranged hit our IsTerrainTile,
	// flag the entity so we can regenerate its terrain.
	//
	// If this entity is still a map-cell, then the 'existingTerrainUpdatingSystem'
	// will take care of reassigning the terrain-tile
	//
	entity.add state.engine.createComponent(NeedsReplacementTerrainTile)
}, { entity, deltaTime ->
	//
	// nothing to do when the IsMapCellRearranged "drops off" this entity
	//
}