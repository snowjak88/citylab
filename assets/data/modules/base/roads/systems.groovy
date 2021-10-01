
//scheduleRoadTileFitting = { entity ->
//
//	final thisCell = isCellMapper.get(entity)
//	final int cx = thisCell.cellX
//	final int cy = thisCell.cellY
//
//	final thisRoad = hasRoadMapper.get(entity)
//
//	final heights = new int[2][2]
//	for(def corner : TileCorner)
//		heights[corner.offsetX][corner.offsetY] = state.map.getCellAltitude(cx, cy, corner)
//
//	final predicates = []
//
//	// That [road] property must contain all this road-cell's edges
//	predicates << { t -> t.ext?.road != null && t.ext?.road?.containsAll(thisRoad.edges) }
//	// This road-cell's edges must contain all the tile's [road]-edges
//	predicates << { t -> t.ext?.road != null && thisRoad.edges.containsAll(t.ext?.road) }
//
//	final pending = entity.addAndReturn( state.engine.createComponent(HasPendingRoadTile) )
//	pending.future = submitResultTask {
//		->
//		tileset.getTileFor heights, predicates, true
//	}
//
//	entity.remove NeedsReplacementRoadTile
//}

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

//timeSliceSystem 'roadTileSchedulingSystem', Family.all(HasRoad, IsMapCell).exclude(HasPendingRoadTile, HasRoadTile).get(), 1/30, { entity, deltaTime ->	 scheduleRoadTileFitting entity }
//
//timeSliceSystem 'roadTileReschedulingSystem', Family.all(HasRoad, IsMapCell, HasRoadTile, NeedsReplacementRoadTile).exclude(HasPendingRoadTile).get(), 1/30, { entity, deltaTime -> scheduleRoadTileFitting entity }
//
//iteratingSystem 'roadTileProcessingSystem', Family.all(HasRoad, HasPendingRoadTile, HasMapLayers).get(), { entity, deltaTime ->
//
//	final pending = hasPendingRoadMapper.get(entity)
//
//	if(!pending.future.isDone())
//		return
//
//	final newTile = pending.future.get()
//
//	final layers = hasLayersMapper.get(entity)
//	layers.tiles['road'] = newTile
//	layers.tints['road'] = null
//	layers.altitudeOverrides['road'] = null
//
//	entity.add state.engine.createComponent(HasRoadTile)
//
//	entity.remove HasPendingRoadTile
//}

//
// There are all sorts of reasons why a road might get destroyed.
//
listeningSystem 'roadCellDestroyedListener', Family.all(HasRoad).one(CellHeightChanged, TerrainTileChanged, IsNonBuildableCell).get(), { entity, deltaTime ->
	
	final layers = hasLayersMapper.get(entity)
	layers?.removeAll 'road'
	
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
		}
	}
	
	entity.remove HasRoad
	//	entity.remove HasPendingRoadTile
	//	entity.remove HasRoadTile
},
{ entity, deltaTime ->
	
}