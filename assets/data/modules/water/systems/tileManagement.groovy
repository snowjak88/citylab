
//
// Any entity tagged as watery, but lacking water-tiles, needs to be scheduled for a fitting!
//
iteratingSystem 'waterTileFitSchedulingSystem', Family.all(IsWateryCell, IsMapCell).exclude(HasPendingWaterTiles, HasWaterTiles).get(), { entity, deltaTime ->
	
	final thisCell = isCellMapper.get(entity)
	final int cx = thisCell.cellX
	final int cy = thisCell.cellY
	
	final thisWater = isWaterCellMapper.get(entity)
	
	final heights = new int[2][2]
	for(def corner : TileCorner)
		heights[corner.offsetX][corner.offsetY] = Util.max( sealevel, state.map.getCellAltitude(cx, cy, corner) )
	
	final predicates = []
	
	//
	// Add a predicate for each corner for which this cell is watery
	thisWater.levels.each { corner, level ->
		if(level > 0)
			predicates << { t -> t.ext.water != null && t.ext.water.contains(corner) }
	}
	
	final pending = entity.addAndReturn( state.engine.createComponent(HasPendingWaterTiles) )
	pending.future = submitResultTask {
		->
		tileset.getMinimalTilesFor heights, predicates, true
	}
}

iteratingSystem 'waterTileFitProcessingSystem', Family.all(IsMapCell, IsWateryCell, HasPendingWaterTiles).get(), { entity, deltaTime ->
	
	final pending = hasPendingWaterTilesMapper.get(entity)
	
	if(!pending.future.isDone())
		return
	
	final newTiles = pending.future.get()
	
	final roadTiles = entity.addAndReturn( state.engine.createComponent( HasWaterTiles ))
	if(newTiles)
		roadTiles.tiles.addAll newTiles
	
	entity.remove HasPendingWaterTiles
}