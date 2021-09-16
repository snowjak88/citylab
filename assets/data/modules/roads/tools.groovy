
buttonGroup 'road-tools', {
	title = i18n.get 'road-tools-group'
}

placeRoad = { float cellX, float cellY ->
	final int cx = cellX
	final int cy = cellY
	
	if(!isValidRoadCell(cx,cy))
		return
	
	final entity = state.map.getEntity(cx, cy)
	if(hasRoadMapper.has(entity))
		entity.remove HasRoad
	
	final hasRoad = entity.addAndReturn( state.engine.createComponent(HasRoad) )
	
	//
	// Scan neighboring cells for roads.
	for(def edge : TileEdge) {
		
		// neighbor-cell coordinates
		final int nx = cx + edge.dx
		final int ny = cy + edge.dy
		if(!state.map.isValidCell(nx,ny))
			continue
		
		final neighbor = state.map.getEntity(nx,ny)
		if(hasRoadMapper.has(neighbor)) {
			
			hasRoad.edges << edge
			hasRoadMapper.get(neighbor).edges << edge.opposite
			
			//
			// Ensure that the neighboring cell has its road-tiles re-fitted
			neighbor.remove HasRoadTile
		}
	}
	
	entity.remove HasRoadTile
}

isValidRoadCell = { int cx, int cy ->
	
	if(!state.map.isValidCell(cx,cy))
		return false
	
	final entity = state.map.getEntity(cx,cy)
	
	def altitude = state.map.getCellAltitude(cx, cy, TileCorner.TOP)
	def dissimilarAltitude = 0
	for(def corner : TileCorner) {
		if(state.map.getCellAltitude(cx, cy, corner) != altitude)
			dissimilarAltitude++
	}
	
	if(dissimilarAltitude % 2 != 0)
		return false
	
	return true
}

tool 'placeRoad', {
	
	title = i18n.get 'road-tools-place'
	
	button {
		buttonUp = 'road-flat-4way.png'
		buttonDown = 'road-flat-4way.png'
		group = 'road-tools'
	}
	
	mapHover { cellX, cellY ->
		mapCellOutliner.active = true
		mapCellOutliner.cellX = cellX
		mapCellOutliner.cellY = cellY
	}
	
	mapClick Buttons.LEFT, placeRoad
	mapDragStart Buttons.LEFT, placeRoad
	mapDragUpdate Buttons.LEFT, placeRoad
}