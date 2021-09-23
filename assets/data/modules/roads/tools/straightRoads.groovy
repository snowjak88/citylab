//
// Attempts to place roads connecting two points with 0 corners.
//
// Assumes that the [roadPlan] data-structure is already populated.
//
planStraightRoad = {
	->
	
	//
	// For each pair of checkpoints, plan a strictly-rectilinear path.
	// If any section of the path cannot work, then the whole path fails.
	//
	roadPlan.pathSuccess = true
	roadPlan.pathDone = true
	
	if(roadPlan.checkpoints.size() < 2)
		return
	
	def startEntity = roadPlan.checkpoints[0]
	def endEntity = roadPlan.checkpoints[1]	
	
	final startCell = isCellMapper.get(startEntity)
	final endCell = isCellMapper.get(endEntity)
	
	//
	//
	final int startX = startCell.cellX, startY = startCell.cellY
	final int endX = endCell.cellX, endY = endCell.cellY
	
	final sx = (int) Math.signum( endX - startX )
	final sy = (int) Math.signum( endY - startY )
	
	final dx = Math.abs(endX - startX)
	final dy = Math.abs(endY - startY)
	
	//
	// Which direction do we go in? Whichever has the larger displacement.
	int fromX = startX, fromY = startY
	
	if(Math.abs(endX - startX) > Math.abs(endY - startY)) {
		//
		// Do X
		//
		
		for(def x=startX; x!=endX; x += sx) {
			
			if(!isValidRoadConnection(fromX, fromY, x, fromY)) {
				roadPlan.pathSuccess = false
				return
			}
			
			roadPlan.pathEntities << state.map.getEntity(x, fromY)
			
			fromX = x
		}
		
		roadPlan.pathEntities << state.map.getEntity(endX, fromY)
		
	} else {
		//
		// Do Y
		//
		
		for(def y=startY; y!=endY; y += sy) {
			
			if(!isValidRoadConnection(fromX, fromY, fromX, y)) {
				roadPlan.pathSuccess = false
				return
			}
			
			roadPlan.pathEntities << state.map.getEntity(fromX, y)
			
			fromY = y
		}
		
		roadPlan.pathEntities << state.map.getEntity(fromX, endY)
	}

	roadPlan.pathEntities.each { it.add state.engine.createComponent(IsSelected) }
}