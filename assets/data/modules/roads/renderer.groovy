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