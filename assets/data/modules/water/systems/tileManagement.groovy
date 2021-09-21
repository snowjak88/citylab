
//
// When a cell is tagged as watery or not, tag it as NonBuildable or not.
//
listeningSystem 'waterTileNonBuildableSystem', Family.all(IsWateryCell).get(), { entity, deltaTime ->
	if(isCellNonBuildableMapper.has(entity))
		isCellNonBuildableMapper.get(entity).blockerIDs << 'water'
	else
		entity.addAndReturn( state.engine.createComponent( IsNonBuildableCell ) ).blockerIDs << 'water'
}, { entity, deltaTime ->
	isCellNonBuildableMapper.get(entity).blockerIDs.remove 'water'
}

//
// Any entity tagged as watery, but lacking water-tiles, needs to be scheduled for a fitting!
//
iteratingSystem 'waterTileFitSchedulingSystem', Family.all(IsWateryCell, IsMapCell).exclude(HasPendingWaterTile, HasWaterTile).get(), { entity, deltaTime ->
	
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
	
	final pending = entity.addAndReturn( state.engine.createComponent(HasPendingWaterTile) )
	pending.future = submitResultTask {
		->
		tileset.getTileFor heights, predicates, true
	}
}

//
// Tiles being fitted must be checked to see if their fitting is complete.
//
iteratingSystem 'waterTileFitProcessingSystem', Family.all(IsMapCell, HasPendingWaterTile, HasMapLayers).get(), { entity, deltaTime ->
	
	final thisCell = isCellMapper.get(entity)
	final int cx = thisCell.cellX
	final int cy = thisCell.cellY
	
	final pending = hasPendingWaterTileMapper.get(entity)
	
	if(!pending.future.isDone())
		return
	
	final newTile = pending.future.get()
	
	final layers = hasLayersMapper.get(entity)
	layers.tiles['water'] = newTile
	layers.tints['water'] = null
	
	if(newTile) {
		final normalAltitude = state.map.getCellAltitude( cx, cy, newTile.base )
		layers.altitudeOverrides['water'] = (normalAltitude < sealevel) ? sealevel : null
		
		entity.add state.engine.createComponent(HasWaterTile)
		
	} else
		entity.remove HasWaterTile
	
	
	entity.remove HasPendingWaterTile
}
