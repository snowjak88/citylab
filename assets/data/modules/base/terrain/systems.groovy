
isCellMapper = ComponentMapper.getFor(IsMapCell)
hasTerrainTypeMapper = ComponentMapper.getFor(HasTerrainType)

onActivate { ->
	for(int x=0; x<state.map.width; x++)
		for(int y=0; y<state.map.height; y++) {
			final entity = state.map.getEntity(x,y)
			if(!entity)
				continue
			entity.add state.engine.createComponent(NeedsReplacementTerrainTile)
		}
}

iteratingSystem 'terrainCharacteristicsUpdatingSystem', Family.all(IsMapCell, NeedsReplacementTerrainTile).exclude(UpdatedCellCharacteristics).get(), { entity, deltaTime ->
	
	final mapCell = isCellMapper.get(entity)
	final int cellX = mapCell.cellX
	final int cellY = mapCell.cellY
	
	def terrainType = 'grass'
	if(hasTerrainTypeMapper.has(entity))
		terrainType = hasTerrainTypeMapper.get(entity).type ?: terrainType
	
	final updated = entity.addAndReturn( state.engine.createComponent(UpdatedCellCharacteristics) )
	
	updated.layerID = 'terrain'
	updated.tileset = tileset
	updated.ext.terrain = terrainType
	for(def corner : TileCorner)
		updated.heights[corner.offsetX][corner.offsetY] = state.map.getCellAltitude(cellX, cellY, corner)
	
	entity.remove NeedsReplacementTerrainTile
	
	entity.add state.engine.createComponent(TerrainTileChanged)
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

