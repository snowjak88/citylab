import javax.management.remote.rmi.RMIConnector.Util

id = 'water'
description = 'Manages dispersion of water throughout the map'

dependsOn 'terrain'

//
// Water-propagation is governed by a few basic mechanics:
//
// * Map-vertices can have a certain amount of water in them.
//   A map-vertex can be fractionally-full of water.
//
// * Water can flow from map-vertices with water into those without or with less.
//   This flow is also fractional, at some fixed rate.
//   Flow occurs faster downhill than on the level.
//
// * Watery vertices may find that they have no possible outflow-destinations,
//  e.g., because all their destinations are too full. In such cases, we
// should mark that vertex so we don't consider it for outflow unnecessarily.
//
// * Map-vertices that have enough water will get rendered as such.
//
//
// These mechanics are implemented by:
//
// HasWater
//   This vertex has at least some water in it.
//   The amount of water is given as a fraction in [0,1]
//   Additionally, the vertex may be an "infinite" source, in which
//   case outflow does not diminish it, nor does inflow increase it.
//
// HasPendingWaterOutflow
//   This vertex has some water scheduled to flow out of it
//   into a specified adjoining vertex.
//
// NoPossibleWaterOutflow
//   This vertex was examined already and we found no possible
//   outflow vertices from it, given its water level.
//   Don't consider this vertex for outflow.
//
//
// WaterOutflowSchedulingSystem
//   Every N seconds, examines those entities that HasWater.
//   If possible, picks an adjoining entity that either has a
//   lower water-level or no water at all. Adds PendingWaterFlow
//   components to schedule the actual water-flow.
//
// WaterOutflowProcessingSystem
//   All entities tagged with HasPendingWaterOutflow get that
//   outflow executed and the Outflow component removed.
//   NOTE that a vertex can be filled to overflowing (i.e., level > 1) with water.
//
// WaterOutflowReenableSystem
//   When a map-cell is marked as IsMapCellRearranged, then all its
//   corresponding vertices should have their NoPossibleOutflow component removed.
//

seaLevel = preferences.getInteger('seaLevel', 0)
normalFlowRate = 0.5
downhillFlowRate = normalFlowRate * 2

class HasWater implements Component, Poolable {
	float level
	boolean infinite
	void reset() {
		level = 0
		infinite = false
	}
}
class HasPendingWaterOutflow implements Component, Poolable {
	float amount
	int toX = -1, toY = -1
	void reset() {
		amount = 0
		toX = -1
		toY = -1
	}
}
class NoPossibleWaterOutflow implements Component, Poolable {
	void reset() { }
}

isCellMapper = ComponentMapper.getFor(IsMapCell)
isVertexMapper = ComponentMapper.getFor(IsMapVertex)
hasWaterMapper = ComponentMapper.getFor(HasWater)
hasPendingOutflowMapper = ComponentMapper.getFor(HasPendingWaterOutflow)

createWaterSource = { int vx, int vy, boolean infinite = false, float level = 1 ->
	
	Entity entity = state.map.getVertexEntity(vx, vy)
	
	HasWater hasWater
	if(hasWaterMapper.has( entity ))
		hasWater = hasWaterMapper.get( entity )
	else {
		hasWater = state.engine.createComponent(HasWater)
		entity.add hasWater
	}
	
	hasWater.level = level
	hasWater.infinite = infinite
}

onActivate {
	->
	
	//
	// ensure that all edge-vertices at or below sea-level are marked as infinite water-sources
	
	final maxX = state.map.width
	final maxY = state.map.height
	
	for(def x=0; x<=maxX; x++)
		for(def y=0; y<=maxY; y++) {
			if(state.map.getVertexAltitude(x,y) <= seaLevel)
				createWaterSource x, y, (x == 0 || y == 0 || x == maxX || y == maxY), 1
		}
}

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
	}
	
	entity.remove HasPendingWaterOutflow
	if(thisWater.level < normalFlowRate)
		entity.remove HasWater
}

intervalIteratingSystem 'waterEvaporationSystem', Family.all(HasWater).exclude(HasPendingWaterOutflow).get(), 5.0, { entity, deltaTime ->
	thisWater = hasWaterMapper.get(entity)
	
	if(thisWater.level < 0.9)
		thisWater.level -= normalFlowRate / 4f
	
	if(thisWater.level <= normalFlowRate)
		entity.remove HasWater
}

listeningSystem 'waterFlavoringSystem', Family.all(HasWater).get(), { entity, deltaTime ->
	
	if(!isVertexMapper.has(entity))
		return
	
	final mapVertex = isVertexMapper.get(entity)
	final int vx = mapVertex.vertexX
	final int vy = mapVertex.vertexY
	
	if( state.map.isValidVertex(vx,vy) ) {
		state.map.addVertexFlavor vx, vy, 'water'
		
		for(def corner : TileCorner.values())
			if(state.map.isValidCell(vx - corner.offsetX, vy - corner.offsetY))
				state.map.getEntity(vx - corner.offsetX, vy - corner.offsetY).add state.engine.createComponent(IsMapCellRearranged)
	}
	
}, { entity, deltaTime ->
	
	if( isVertexMapper.has(entity) ) {
		
		final mapVertex = isVertexMapper.get(entity)
		final int vx = mapVertex.vertexX
		final int vy = mapVertex.vertexY
		
		if( state.map.isValidVertex(vx,vy) ) {
			state.map.removeVertexFlavor vx, vy, 'water'
			for(def corner : TileCorner.values())
				if(state.map.isValidCell(vx - corner.offsetX, vy - corner.offsetY))
					state.map.getEntity(vx - corner.offsetX, vy - corner.offsetY).add state.engine.createComponent(IsMapCellRearranged)
		}
		
	}
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