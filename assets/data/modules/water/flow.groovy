

windowIteratingSystem 'waterOutflowSchedulingSystem', Family.all(IsMapVertex, HasWater).exclude(HasPendingWaterOutflow, NoPossibleWaterOutflow).get(), 32, { entity, deltaTime ->
	
	final mapVertex = isVertexMapper.get(entity)
	final int vx = mapVertex.vertexX
	final int vy = mapVertex.vertexY
	
	if(!state.map.isValidVertex(vx,vy))
		return
	
	final thisWater = hasWaterMapper.get(entity)
	final thisAlt = state.map.getVertexAltitude(vx,vy)
	
	//
	// Scan neighboring vertices for eligible outflows
	final validOutflows = []
	for(def neighbor : [
				[-1, 0],
				[+1, 0],
				[0, -1],
				[0, +1]
			]) {
		// "neighbor" coordinates
		def (int dx, int dy) = neighbor
		final nx = vx + dx
		final ny = vy + dy
		
		if(!state.map.isValidVertex(nx, ny))
			continue
		
		//
		// Is the neighbor vertex above this vertex?
		final neighborAlt = state.map.getVertexAltitude(nx,ny)
		if(neighborAlt > thisAlt)
			continue
		
		//
		// Is the neighbor level with us and have at least as much water?
		boolean canFlowIntoNeighbor = false
		final neighborEntity = state.map.getVertexEntity(nx, ny)
		if(!hasWaterMapper.has(neighborEntity))
			canFlowIntoNeighbor = true
		else {
			final neighborWater = hasWaterMapper.get(neighborEntity)
			canFlowIntoNeighbor = (neighborAlt < thisAlt) || (neighborAlt == thisAlt && neighborWater.level < thisWater.level)
		}
		
		if(!canFlowIntoNeighbor)
			continue
		
		//
		// OK. We can outflow into this vertex.
		validOutflows << [nx, ny]
		//
		// If this is a downhill outflow, we get two chances to select it
		if(neighborAlt < thisAlt)
			validOutflows << [nx, ny]
	}
	
	if(validOutflows.isEmpty()) {
		entity.add state.engine.createComponent(NoPossibleWaterOutflow)
		return
	}
	
	//
	// Pick one of the neighbors to outflow into.
	final randomIndex = state.rnd.nextInt(validOutflows.size())
	def (int outflowX, int outflowY) = validOutflows[randomIndex]
	
	//
	// Is this a level- or downhill-flow?
	final outflowAlt = state.map.getVertexAltitude(outflowX, outflowY)
	final isLevelFlow = (thisAlt == outflowAlt)
	
	//
	// Create the "pending-outflow" component for this vertex.
	final pendingOutflow = state.engine.createComponent(HasPendingWaterOutflow)
	pendingOutflow.amount = Util.clamp( (isLevelFlow) ? normalFlowRate : downhillFlowRate, 0, thisWater.level )
	pendingOutflow.toX = outflowX
	pendingOutflow.toY = outflowY
	entity.add pendingOutflow
}

iteratingSystem 'waterOutflowProcessingSystem', Family.all(IsMapVertex, HasWater, HasPendingWaterOutflow).get(), { entity, deltaTime ->
	thisVertex = isVertexMapper.get(entity)
	final int vx = thisVertex.vertexX
	final int vy = thisVertex.vertexY
	
	thisWater = hasWaterMapper.get(entity)
	outflow = hasPendingOutflowMapper.get(entity)
	
	if( state.map.isValidVertex(outflow.toX, outflow.toY) ) {
		final outflowEntity = state.map.getVertexEntity( outflow.toX, outflow.toY )
		HasWater outflowWater
		if(hasWaterMapper.has(outflowEntity))
			outflowWater = hasWaterMapper.get(outflowEntity)
		else
			outflowWater = outflowEntity.addAndReturn( state.engine.createComponent(HasWater) )
		
		def toAdd = outflow.amount
		def toSubtract = outflow.amount
		if(thisWater.infinite)
			toSubtract = 0
		if(outflowWater.infinite)
			toAdd = 0
		
		thisWater.level -= toSubtract
		outflowWater.level += toAdd
		
		if(outflowWater.level > 1)
			outflowWater.infinite = true
		
		outflowEntity.remove NoPossibleWaterOutflow
		
		//
		// Notify all cells neighboring the outflow vertex that they should
		// re-examine their water-tiles
		markVertexCellsForWaterTileFitting(outflow.toX, outflow.toY)
	}
	
	entity.remove HasPendingWaterOutflow
	
	//
	// Notify all cells neighboring this vertex that they should
	// re-examine their water-tiles
	markVertexCellsForWaterTileFitting(vx, vy)
	
	if(thisWater.level < normalFlowRate)
		entity.remove HasWater
}

listeningSystem 'waterOutflowReenablingSystem', Family.all(IsMapCell, IsMapCellRearranged).get(), { entity, deltaTime ->
	//
	// Ensure all neighboring watery vertices can be re-checked for outflow.
	final mapCell = isCellMapper.get(entity)
	final int cellX = mapCell.cellX
	final int cellY = mapCell.cellY
	
	for(def corner : TileCorner.values()) {
		
		final int vx = cellX + corner.offsetX
		final int vy = cellY + corner.offsetY
		if(!state.map.isValidVertex(vx, vy))
			continue
		final vertexEntity = state.map.getVertexEntity(vx, vy)
		if(hasWaterMapper.has(vertexEntity))
			vertexEntity.remove NoPossibleWaterOutflow
	}
	
}, { entity, deltaTime ->
	//
	// Nothing to do when an entity drops off this family
}