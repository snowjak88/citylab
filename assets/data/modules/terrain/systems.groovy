
isCellMapper = ComponentMapper.getFor(IsMapCell)
hasLayersMapper = ComponentMapper.getFor(HasMapLayers)

pendingTerrainTileMapper = ComponentMapper.getFor(PendingTerrainTile)

//
// Any map-cell that doesn't already have a terrain-tile should get one!
//
iteratingSystem 'newTerrainFittingSystem', Family.all(IsMapCell).exclude(PendingTerrainTile, HasTerrainTile).get(), { entity, deltaTime ->
	final mapCell = isCellMapper.get(entity)
	final int cellX = mapCell.cellX
	final int cellY = mapCell.cellY
	
	final pendingTerrain = state.engine.createComponent(PendingTerrainTile)
	
	
	for(def corner : TileCorner)
		pendingTerrain.heights[corner.offsetX][corner.offsetY] = state.map.getCellAltitude(cellX, cellY, corner)
	
	pendingTerrain.future = submitResultTask {
		->
		tileset.getTileFor pendingTerrain.heights, [
			{ t ->
				t.ext.terrain == 'grass' }
		]
	}
	entity.add pendingTerrain
	
}

iteratingSystem 'existingTerrainUpdatingSystem', Family.all(IsMapCell, NeedsReplacementTerrainTile).exclude(PendingTerrainTile).get(), { entity, deltaTime ->
	
	final mapCell = isCellMapper.get(entity)
	final int cellX = mapCell.cellX
	final int cellY = mapCell.cellY
	
	final pendingTerrain = state.engine.createComponent(PendingTerrainTile)
	
	for(def corner : TileCorner)
		pendingTerrain.heights[corner.offsetX][corner.offsetY] = state.map.getCellAltitude(cellX, cellY, corner)
	
	pendingTerrain.future = submitResultTask {
		->
		tileset.getTileFor pendingTerrain.heights, [
			{ t ->
				t.ext.terrain == 'grass' }
		]
	}
	entity.add pendingTerrain
	
	entity.remove NeedsReplacementTerrainTile
	entity.remove HasTerrainTile
}

iteratingSystem 'pendingTerrainUpdatingSystem', Family.all(PendingTerrainTile, HasMapLayers).get(), { entity, deltaTime ->
	
	final pendingTerrain = pendingTerrainTileMapper.get(entity)
	if(!pendingTerrain.future.isDone())
		return
	
	final layer = hasLayersMapper.get(entity)
	final newTile = pendingTerrain.future.get()
	
	layer.tiles['terrain'] = newTile
	layer.tints['terrain'] = null
	layer.altitudeOverrides['terrain'] = null
	
	if(newTile)
		entity.add state.engine.createComponent(HasTerrainTile)
	else
		entity.remove HasTerrainTile
	
	entity.add state.engine.createComponent(TerrainTileChanged)
	entity.remove PendingTerrainTile
	
}


//
// Set up "event-Component" handling.
// Event-Components should persist on any Entity for only 1 cycle.
// We need to set up EventComponentSystems for each of our 3 event-Components.
//
// Because these Components should be shared with other Modules, these Components
// cannot be defined inside this Module. Instead, they are defined under:
//   /modules/shared/terrain/
//
eventComponent CellHeightChanged
eventComponent VertexHeightChanged
eventComponent TerrainTileChanged


//
// When a map-cell is "rearranged" -- i.e., it changes a corner-height, or flavor, or whatever --
// we need to make sure that we re-assign the terrain-tile.
//.exclude(NeedsReplacementTerrainTile, PendingTerrainTile)
listeningSystem 'terrainRearrangementSystem', Family.all(CellHeightChanged).get(), { entity, deltaTime ->
	//
	// When we "hear" the IsMapCellRearranged hit a terrain-tile,
	// flag the entity so we can regenerate its terrain.
	//
	// If this entity is still a map-cell, then the 'existingTerrainUpdatingSystem'
	// will take care of reassigning the terrain-tile
	//
	final mapCell = isCellMapper.get(entity)
	final int cellX = mapCell.cellX
	final int cellY = mapCell.cellY
	entity.add state.engine.createComponent(NeedsReplacementTerrainTile)
}, { entity, deltaTime ->
	//
	// nothing to do when the IsMapCellRearranged "drops off" this entity
	//
}

