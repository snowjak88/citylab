
timeSliceSystem 'roadTileSchedulingSystem', Family.all(HasRoad, IsMapCell).exclude(HasPendingRoadTile, HasRoadTile).get(), 1/30, { entity, deltaTime ->
	
	final thisCell = isCellMapper.get(entity)
	final int cx = thisCell.cellX
	final int cy = thisCell.cellY
	
	final thisRoad = hasRoadMapper.get(entity)
	
	final heights = new int[2][2]
	for(def corner : TileCorner)
		heights[corner.offsetX][corner.offsetY] = state.map.getCellAltitude(cx, cy, corner)
	
	final predicates = []
	
	// That [road] property must contain all this road-cell's edges
	predicates << { t -> t.ext?.road != null && t.ext?.road?.containsAll(thisRoad.edges) }
	// This road-cell's edges must contain all the tile's [road]-edges
	predicates << { t -> t.ext?.road != null && thisRoad.edges.containsAll(t.ext?.road) }
	
	final pending = entity.addAndReturn( state.engine.createComponent(HasPendingRoadTile) )
	pending.future = submitResultTask {
		->
		tileset.getTileFor heights, predicates, true
	}
	
}

iteratingSystem 'roadTileProcessingSystem', Family.all(HasRoad, HasPendingRoadTile).get(), { entity, deltaTime ->
	
	final pending = hasPendingRoadMapper.get(entity)
	
	if(!pending.future.isDone())
		return
	
	final newTile = pending.future.get()
	
	entity.addAndReturn( state.engine.createComponent( HasRoadTile )).tile = newTile
	
	entity.remove HasPendingRoadTile
}

listeningSystem 'roadCellRearrangedListener', Family.all(IsMapCellRearranged).one(HasRoad, HasPendingRoadTile, HasRoadTile).get(), { entity, deltaTime ->
	entity.remove HasRoad
	entity.remove HasPendingRoadTile
	entity.remove HasRoadTile
},
{ entity, deltaTime ->
	
}