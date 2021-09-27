
buttonGroup 'road-tools', {
	title = i18n.get 'road-tools-group'
}

mapModes['default'].tools << 'placeRoad'

placeRoad = { float cellX, float cellY ->
	final int cx = cellX
	final int cy = cellY
	
	if(!isValidRoadCell(cx,cy))
		return
	
	final entity = state.map.getEntity(cx, cy)
	if(hasRoadMapper.has(entity))
		entity.remove HasRoad
	
	final hasRoad = entity.addAndReturn( state.engine.createComponent( HasRoad) )
	final networkNode = entity.addAndReturn( state.engine.createComponent( IsNetworkNode ) )
	
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
			networkNode.connections << neighbor
			
			hasRoadMapper.get(neighbor)?.edges?.add edge.opposite
			isNetworkNodeMapper.get(neighbor)?.connections?.add entity
			
			//
			// Ensure that the neighboring cell has its road-tile re-fitted
			neighbor.add state.engine.createComponent(NeedsReplacementRoadTile)
			neighbor.add state.engine.createComponent(RoadCellUpdated)
		}
	}
	
	entity.add state.engine.createComponent(RoadCellUpdated)
	
	entity.add state.engine.createComponent(NeedsReplacementRoadTile)
}

roadPlan = [
	startX: -1,
	startY: -1,
	startEntity: null,
	endX: -1,
	endY: -1,
	endEntity: null,
	onlyStraight: true,
	doPathfinding: false,
	checkpoints: [],
	pathEntities: [],
	pathDone: false,
	pathSuccess: false
]

include 'straightRoads.groovy'
include 'orthogonalRoads.groovy'
include 'pathfoundRoads.groovy'

planRoad = { float cellX, float cellY ->
	
	final int cx = (int) cellX
	final int cy = (int) cellY
	if(!state.map.isValidCell(cx,cy))
		return
	
	if(roadPlan.startX < 0) {
		
		roadPlan.startX = cx
		roadPlan.startY = cy
		
		roadPlan.startEntity = state.map.getEntity(cx,cy)
		if(!roadPlan.startEntity)
			throw new RuntimeException("Given map-cell ($cx,$cy) has no entity!")
		if(!isCellMapper.has(roadPlan.startEntity))
			throw new RuntimeException("Given map-cell ($cx,$cy) has an entity, but is not configured as a map-cell!")
		
		return
	}
	
	if(cx == roadPlan.startX && cy == roadPlan.startY)
		return
	if(cx == roadPlan.endX && cy == roadPlan.endY)
		return
	
	roadPlan.endX = cx
	roadPlan.endY = cy
	roadPlan.endEntity = state.map.getEntity(cx,cy)
	
	roadPlan.checkpoints.clear()
	roadPlan.checkpoints << roadPlan.startEntity
	roadPlan.checkpoints << roadPlan.endEntity
	
	if(roadPlan.pathEntities?.size() > 0) {
		roadPlan.pathEntities.each { it.remove IsSelected }
		roadPlan.pathEntities.clear()
	}
	
	roadPlan.pathSuccess = false
	
	if (roadPlan.doPathfinding && !roadPlan.onlyStraight)
		planPathfoundRoad()
	else if(!roadPlan.onlyStraight)
		planOrthogonalRoad()
	else
		planStraightRoad()
}

hoverX = -1
hoverY = -1
hoverEntity = null

tool 'placeRoad', {
	
	title = i18n.get 'road-tools-place'
	
	button {
		buttonUp = 'road-flat-4way.png'
		buttonDown = 'road-flat-4way.png'
		group = 'road-tools'
	}
	
	modifier SHIFT, {
		->
		roadPlan.onlyStraight = false
	}, {
		->
		roadPlan.onlyStraight = true
	}
	
	modifier CONTROL, {
		->
		roadPlan.doPathfinding = true
	}, {
		->
		roadPlan.doPathfinding = false
	}
	
	mapHover { cellX, cellY ->
		final int cx = cellX, cy = cellY
		
		if(!state.map.isValidCell(cx,cy)) {
			hoverEntity?.remove IsSelected
			hoverEntity = null
		} else
			if(hoverX != cx || hoverY != cy) {
				hoverEntity?.remove IsSelected
				hoverEntity = state.map.getEntity(cx,cy)
				final select = hoverEntity.addAndReturn state.engine.createComponent(IsSelected)
				if(!isValidRoadCell(cx,cy))
					select.status = IsSelected.Status.INVALID
			}
		
		hoverX = cx
		hoverY = cy
	}
	
	mapClick Buttons.LEFT, placeRoad
	mapDragStart Buttons.LEFT, { cellX, cellY ->
		roadPlan.startX = -1
		roadPlan.startY = -1
		roadPlan.endX = -1
		roadPlan.endY = -1
		planRoad cellX, cellY
	}
	mapDragUpdate Buttons.LEFT, planRoad
	mapDragEnd Buttons.LEFT, { float cellX, float cellY ->
		if(roadPlan.pathSuccess) {
			for(def entity : roadPlan.pathEntities) {
				final mapCell = isCellMapper.get(entity)
				placeRoad mapCell.cellX, mapCell.cellY
				entity.remove IsSelected
			}
		}
	}
	
	whileActive {
		->
		if(roadPlan.doPathfinding && !roadPlan.pathDone)
			updatePathfinder()
	}
	
	inactive { ->
		if(hoverEntity)
			hoverEntity.remove IsSelected
	}
}