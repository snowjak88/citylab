
buttonGroup 'road-tools', {
	title = i18n.get 'road-tools-group'
}

onActivate {
	->
	modules['network'].networkLegend.register IsRoadNetworkNode, i18n.get('network-name')
}

mapModes['default'].tools << 'placeRoad'

placeRoad = { Entity from, Entity to ->
	
	if(!isCellMapper.has(from))
		return
	if(!isCellMapper.has(to))
		return
	
	final fromCell = isCellMapper.get(from)
	final toCell = isCellMapper.get(to)
	
	if(!fromCell)
		return
	if(!toCell)
		return
	
	final int fromX = fromCell.cellX
	final int fromY = fromCell.cellY
	final int toX = toCell.cellX
	final int toY = toCell.cellY
	
	if(!isValidRoadCell(fromX,fromY))
		return
	if(!isValidRoadCell(toX,toY))
		return
	
	if(!isValidRoadConnection(fromX, fromY, toX, toY))
		return
	
	def fromRoad = isRoadMapper.get(from)
	if(!fromRoad)
		fromRoad = from.addAndReturn( state.engine.createComponent(IsRoadNetworkNode) )
	
	def toRoad = isRoadMapper.get(to)
	if(!toRoad)
		toRoad = to.addAndReturn( state.engine.createComponent(IsRoadNetworkNode) )
	
	//
	// Mark the two cells as connected
	final int dx = toX - fromX
	final int dy = toY - fromY
	final fromToEdge = TileEdge.fromDelta(dx,dy)
	if(!fromToEdge)
		return
	fromRoad.connections << to
	
	toRoad.connections << from
	
	from.add state.engine.createComponent(RoadCellUpdated)
	from.add state.engine.createComponent(NeedsReplacementRoadTile)
	
	to.add state.engine.createComponent(RoadCellUpdated)
	to.add state.engine.createComponent(NeedsReplacementRoadTile)
	
	modules['network'].checkNetworkID to, IsRoadNetworkNode
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
			if(roadPlan.pathEntities.size() > 1) {
				def fromEntity = roadPlan.pathEntities[0]
				for(def entity : roadPlan.pathEntities) {
					if(entity === fromEntity)
						continue
					
					placeRoad fromEntity, entity
					fromEntity = entity
					
					entity.remove IsSelected
				}
			}
		}
	}
	
	whileActive {
		->
		if(roadPlan.doPathfinding && !roadPlan.pathDone)
			updatePathfinder()
	}
	
	inactive {
		->
		if(hoverEntity)
			hoverEntity.remove IsSelected
	}
}