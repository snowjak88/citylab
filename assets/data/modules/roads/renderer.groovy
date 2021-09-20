cellRenderHook 'roadRender', { delta, cellX, cellY, support ->
	final entity = state.map.getEntity(cellX, cellY)
	if(entity)
		if(hasRoadTileMapper.has(entity)) {
			final roadTile = hasRoadTileMapper.get(entity)
			
			for(def tile in roadTile.tiles)
				support.renderTile cellX, cellY, tile
		}
} after 'terrainRender' before 'waterRender'

customRenderHook 'roadPlanRender', { delta, batch, shapeDrawer, renderingSupport ->
	
	if(roadPlan.roadPlanDrawn)
		return
	
	if(!roadPlan.currentPathfindRequest?.done)
		return
	
	mapCellListOutliner.active = true
	for(def node : roadPlan.currentPathfindRequest.result) {
		mapCellListOutliner.cellX << node.cellX
		mapCellListOutliner.cellY << node.cellY
	}
	mapCellListOutliner.color = (roadPlan.currentPathfindRequest.success) ? null : Color.SCARLET
	roadPlan.roadPlanDrawn = true
} after 'terrainRender'