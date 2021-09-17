
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
		if(hasRoadMapper.has(neighbor) && isValidRoadConnection(cx, cy, nx, ny)) {
			
			hasRoad.edges << edge
			hasRoadMapper.get(neighbor).edges << edge.opposite
			
			//
			// Ensure that the neighboring cell has its road-tiles re-fitted
			neighbor.remove HasRoadTile
		}
	}
	
	entity.remove HasRoadTile
}

roadPlan = [
	roadPlanStartX: -1,
	roadPlanStartY: -1,
	roadPlanEndX: -1,
	roadPlanEndY: -1,
	penalizeCorners: false,
	roadPlanStartEntity: null,
	roadPlanStartCell: null,
	currentPathfindRequest: null,
	roadPlanDrawn: true
]

planRoad = { float cellX, float cellY ->
	
	final int cx = (int) cellX
	final int cy = (int) cellY
	if(!state.map.isValidCell(cx,cy))
		return
	
	if(roadPlan.roadPlanStartX < 0) {
		
		roadPlan.roadPlanStartX = cx
		roadPlan.roadPlanStartY = cy
		
		roadPlanStartEntity = state.map.getEntity(cx,cy)
		if(!roadPlanStartEntity)
			throw new RuntimeException("Given map-cell ($cx,$cy) has no entity!")
		if(!isCellMapper.has(roadPlanStartEntity))
			throw new RuntimeException("Given map-cell ($cx,$cy) has an entity, but is not configured as a map-cell!")
		roadPlanStartCell = isCellMapper.get(roadPlanStartEntity)
		
		return
	}
	
	if(cx == roadPlan.roadPlanStartX && cy == roadPlan.roadPlanStartY)
		return
	if(cx == roadPlan.roadPlanEndX && cy == roadPlan.roadPlanEndY)
		return
	
	final endEntity = state.map.getEntity(cx,cy)
	if(!endEntity)
		throw new RuntimeException("Given map-cell ($cx,$cy) has no entity!")
	if(!isCellMapper.has(endEntity))
		throw new RuntimeException("Given map-cell ($cx,$cy) has an entity, but is not configured as a map-cell!")
	endCell = isCellMapper.get(endEntity)
	
	roadPlan.roadPlanEndX = cx
	roadPlan.roadPlanEndY = cy
	
	if(roadPlan.currentPathfindRequest)
		Pools.free(roadPlan.currentPathfindRequest)
	
	roadPlan.currentPathfindRequest = Pools.obtain(PathfindRequest)
	roadPlan.currentPathfindRequest.start = roadPlanStartCell
	roadPlan.currentPathfindRequest.end = endCell
	
	roadPlan.roadPlanDrawn = false
	
	mapCellListOutliner.cellX.clear()
	mapCellListOutliner.cellY.clear()
	
	submitPathfindRequest roadPlan.currentPathfindRequest
}

tool 'placeRoad', {
	
	title = i18n.get 'road-tools-place'
	
	button {
		buttonUp = 'road-flat-4way.png'
		buttonDown = 'road-flat-4way.png'
		group = 'road-tools'
	}
	
	modifier SHIFT, { ->
		roadPlan.penalizeCorners = true
	}, { ->
		roadPlan.penalizeCorners = false
	}
	
	mapHover { cellX, cellY ->
		mapCellOutliner.active = true
		mapCellOutliner.cellX = cellX
		mapCellOutliner.cellY = cellY
		mapCellOutliner.color = (isValidRoadCell( (int) cellX, (int) cellY)) ? null : Color.SCARLET
	}
	
	mapClick Buttons.LEFT, placeRoad
	mapDragStart Buttons.LEFT, { cellX, cellY ->
		roadPlan.roadPlanStartX = -1
		roadPlan.roadPlanStartY = -1
		roadPlan.roadPlanEndX = -1
		roadPlan.roadPlanEndY = -1
		planRoad cellX, cellY
	}
	mapDragUpdate Buttons.LEFT, planRoad
	mapDragEnd Buttons.LEFT, { float cellX, float cellY ->
		if(roadPlan.currentPathfindRequest?.done && roadPlan.currentPathfindRequest?.success) {
			
			for(def node : roadPlan.currentPathfindRequest.result)
				placeRoad node.cellX, node.cellY
			
			Pools.free roadPlan.currentPathfindRequest
		}
		
		mapCellListOutliner.active = false
	}
}