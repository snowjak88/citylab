//
// Attempts to place roads connecting two or more points with no more than 1 corner in each connecting segment.
//
// Assumes that the [roadPlan] data-structure is already populated.
//
planOrthogonalRoad = {
	->
	
	//
	// For each pair of checkpoints, plan a strictly-rectilinear path.
	// If any section of the path cannot work, then the whole path fails.
	//
	roadPlan.pathSuccess = true
	roadPlan.pathDone = true
	
	if(roadPlan.checkpoints.isEmpty())
		return
	
	def startEntity = roadPlan.checkpoints[0]
	for(def endEntity : roadPlan.checkpoints) {
		if(startEntity === endEntity)
			continue
		
		final startCell = isCellMapper.get(startEntity)
		final endCell = isCellMapper.get(endEntity)
		
		//
		//
		final int startX = startCell.cellX, startY = startCell.cellY
		final int endX = endCell.cellX, endY = endCell.cellY
		
		final sx = (int) Math.signum( endX - startX )
		final sy = (int) Math.signum( endY - startY )
		
		//
		// Which direction do we go in first? Whichever has the larger displacement.
		int fromX = startX, fromY = startY
		
		if(Math.abs(endX - startX) > Math.abs(endY - startY)) {
			//
			// Do X first, then Y
			//
			
			for(def x=startX; x!=endX; x += sx) {
				
				if(!isValidRoadConnection(fromX, fromY, x, fromY)) {
					roadPlan.pathSuccess = false
					return
				}
				
				roadPlan.pathEntities << state.map.getEntity(x, fromY)
				fromX = x
			}
			
			fromX = endX
			
			for(def y=startY; y!=endY; y += sy) {
				
				if(!isValidRoadConnection(fromX, fromY, fromX, y)) {
					roadPlan.pathSuccess = false
					return
				}
				
				roadPlan.pathEntities << state.map.getEntity(fromX, y)
				fromY = y
			}
			
		} else {
			//
			// Do Y first, then X
			//
			
			for(def y=startY; y!=endY; y += sy) {
				
				if(!isValidRoadConnection(fromX, fromY, fromX, y)) {
					roadPlan.pathSuccess = false
					return
				}
				
				roadPlan.pathEntities << state.map.getEntity(fromX, y)
				fromY = y
			}
			
			fromY = endY
			
			for(def x=startX; x!=endX; x += sx) {
				
				if(!isValidRoadConnection(fromX, fromY, x, fromY)) {
					roadPlan.pathSuccess = false
					return
				}
				
				roadPlan.pathEntities << state.map.getEntity(x, fromY)
				fromX = x
			}
		}
		
		roadPlan.pathEntities << state.map.getEntity(endX, endY)
	}
	
	roadPlan.pathEntities.each { it.add state.engine.createComponent(IsSelected) }
}