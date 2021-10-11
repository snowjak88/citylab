
iteratingSystem 'roadTileCharacteristicsUpdatingSystem', Family.all(IsMapCell, NeedsReplacementRoadTile).exclude(UpdatedCellCharacteristics).get(), { entity, deltaTime ->
	
	final thisCell = isCellMapper.get(entity)
	final int cx = thisCell.cellX
	final int cy = thisCell.cellY
	
	final thisRoad = isRoadMapper.get(entity)
	
	final updated = entity.addAndReturn( state.engine.createComponent(UpdatedCellCharacteristics) )
	
	updated.layerID = 'road'
	updated.tileset = tileset
	updated.searchSeparately = tileset.ext.roads?.roadsAsLayer
	
	if(thisRoad) {
		updated.ext.road = []
		thisRoad.connections.each { c ->
			final connectionCell = isCellMapper.get c
			if(!connectionCell)
				return
			updated.ext.road.add TileEdge.fromDelta( (int)(connectionCell.cellX - cx), (int)(connectionCell.cellY - cy) )
		}
	}
	
	entity.remove NeedsReplacementRoadTile
	
}

//
// There are all sorts of reasons why a road might get destroyed.
//
listeningSystem 'roadCellDestroyedListener', Family.all(IsRoadNetworkNode).one(CellHeightChanged, TerrainTileChanged, IsNonBuildableCell).get(), { entity, deltaTime ->
	
	//
	// Any roads that point to this cell should no longer point to it
	//
	if(hasRoadMapper.has(entity) && isCellMapper.has(entity)) {
		final road = isRoadMapper.get(entity)
		final thisCell = isCellMapper.get(entity)
		final int cx = thisCell.cellX
		final int cy = thisCell.cellY
		
		road.connections.each { c ->
			final connectionRoad = isRoadMapper.get c
			if(!connectionRoad)
				return
			connectionRoad.remove entity
			c.add state.engine.createComponent( NeedsReplacementRoadTile )
		}
	}
	
	entity.remove IsRoadNetworkNode
	entity.add state.engine.createComponent( NeedsReplacementRoadTile )
},
{ entity, deltaTime ->
	
}