
iteratingSystem 'roadTileCharacteristicsUpdatingSystem', Family.all(IsMapCell, NeedsReplacementRoadTile).exclude(UpdatedCellCharacteristics).get(), { entity, deltaTime ->
	
	final thisCell = isCellMapper.get(entity)
	final int cx = thisCell.cellX
	final int cy = thisCell.cellY
	
	final thisRoad = hasRoadMapper.get(entity)
	
	final updated = entity.addAndReturn( state.engine.createComponent(UpdatedCellCharacteristics) )
	
	updated.layerID = 'road'
	updated.tileset = tileset
	updated.searchSeparately = tileset.ext.roads?.roadsAsLayer
	
	if(thisRoad) {
		updated.ext.road = []
		updated.ext.road.addAll thisRoad.edges
	}
	
	entity.remove NeedsReplacementRoadTile
	
}

//
// There are all sorts of reasons why a road might get destroyed.
//
listeningSystem 'roadCellDestroyedListener', Family.all(HasRoad).one(CellHeightChanged, TerrainTileChanged, IsNonBuildableCell).get(), { entity, deltaTime ->
	
	//
	// Any roads that point to this cell should no longer point to it
	//
	if(hasRoadMapper.has(entity) && isCellMapper.has(entity)) {
		final road = hasRoadMapper.get(entity)
		final thisCell = isCellMapper.get(entity)
		final int cx = thisCell.cellX
		final int cy = thisCell.cellY
		for(def edge : road.edges) {
			final int nx = cx + edge.dx
			final int ny = cy + edge.dy
			if(!state.map.isValidCell(nx,ny))
				continue
			final neighbor = state.map.getEntity(nx,ny)
			
			if(!hasRoadMapper.has(neighbor))
				continue
			hasRoadMapper.get(neighbor).edges.remove edge.opposite
			
			neighbor.add state.engine.createComponent( NeedsReplacementRoadTile )
		}
	}
	
	entity.remove HasRoad
	entity.add state.engine.createComponent( NeedsReplacementRoadTile )
},
{ entity, deltaTime ->
	
}