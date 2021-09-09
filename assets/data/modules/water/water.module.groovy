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
//   The vertex may be an "infinite" source, in which case outflow
//   does not diminish it, nor does inflow increase it.
//
// IsConnectedToInfiniteWater
//   This vertex is connected (at whatever remove) to an infinite-
//   water vertex.
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

seaLevel = preferences.getInteger('seaLevel', 1)
normalFlowRate = 0.5
downhillFlowRate = normalFlowRate * 2

tilesetName = preferences.getString('tileset-name', 'default')
dependsOn tilesetName, TileSet
tileset = assets.getByID tilesetName, TileSet

class HasWater implements Component, Poolable {
	float level
	void reset() {
		level = 0
	}
}
class IsInfiniteWater implements Component, Poolable {
	void reset() { }
}
class IsConnectedToInfiniteWater implements Component, Poolable {
	int toX, toY
	void reset() {
		toX = -1
		toY = -1
	}
}
class NeedsReplacementWaterTiles implements Component, Poolable {
	final EnumSet<TileCorner> corners = EnumSet.noneOf(TileCorner)
	void reset() {
		corners.clear()
	}
}
class HasPendingWaterTiles implements Component, Poolable {
	final EnumMap<TileCorner,ListenableFuture<List<Tile>>> futures = new EnumMap(TileCorner)
	void reset() {
		futures.clear()
	}
}
class HasWaterTiles implements Component, Poolable {
	final EnumMap<TileCorner,List<Tile>> tiles = new EnumMap(TileCorner)
	void reset() {
		tiles.clear()
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
needsReplacementMapper = ComponentMapper.getFor(NeedsReplacementWaterTiles)
hasPendingTilesMapper = ComponentMapper.getFor(HasPendingWaterTiles)
hasWaterTilesMapper = ComponentMapper.getFor(HasWaterTiles)
hasPendingOutflowMapper = ComponentMapper.getFor(HasPendingWaterOutflow)

createWaterSource = { int vx, int vy, boolean infinite = false, float level = 1 ->
	
	Entity entity = state.map.getVertexEntity(vx, vy)
	
	HasWater hasWater
	if(hasWaterMapper.has( entity ))
		hasWater = hasWaterMapper.get( entity )
	else {
		hasWater = entity.addAndReturn( state.engine.createComponent(HasWater) )
	}
	
	hasWater.level = level
	
	if(infinite)
		entity.add state.engine.createComponent( IsInfiniteWater )
	
	markVertexCellsForWaterTileFitting vx, vy
}

markVertexCellsForWaterTileFitting = { int vx, int vy ->
	final vertexEntity = state.map.getVertexEntity(vx, vy)
	final isWatery = hasWaterMapper.has(vertexEntity)
	
	for(def corner : TileCorner.values()) {
		final int cx = vx - corner.offsetX
		final int cy = vy - corner.offsetY
		if(!state.map.isValidCell(cx, cy))
			continue
		
		final cellEntity = state.map.getEntity(cx, cy)
		def replacementTiles
		if(needsReplacementMapper.has(cellEntity))
			replacementTiles = needsReplacementMapper.get(cellEntity)
		else
			replacementTiles = cellEntity.addAndReturn( state.engine.createComponent(NeedsReplacementWaterTiles) )
		
		replacementTiles.corners << corner
	}
}

onActivate {
	->
	
	//
	// ensure that all vertices at or below sea-level are marked as water-sources
	// *edge* vertices should be *infinite*
	final maxX = state.map.width
	final maxY = state.map.height
	
	for(def x=0; x<=maxX; x++)
		for(def y=0; y<=maxY; y++) {
			if(state.map.getVertexAltitude(x,y) <= seaLevel)
				createWaterSource x, y, (x == 0 || y == 0 || x == maxX || y == maxY), 1
		}
}

include 'flow.groovy'

//intervalIteratingSystem 'waterEvaporationSystem', Family.all(IsMapVertex, HasWater).exclude(HasPendingWaterOutflow).get(), 5.0, { entity, deltaTime ->
//	final thisVertex = isVertexMapper.get(entity)
//	final int vx = thisVertex.vertexX
//	final int vy = thisVertex.vertexY
//	final thisWater = hasWaterMapper.get(entity)
//	
//	if(thisWater.infinite)
//		return
//	
//	if(thisWater.level < 0.9)
//		thisWater.level -= normalFlowRate / 4f
//	
//	if(thisWater.level <= normalFlowRate)
//		entity.remove HasWater
//		
//	markVertexCellsForWaterTileFitting vx, vy
//}


iteratingSystem 'waterTileFittingSchedulingSystem', Family.all(IsMapCell, NeedsReplacementWaterTiles).exclude(HasPendingWaterTiles).get(), { entity, deltaTime ->
	final mapCell = isCellMapper.get(entity)
	final int cx = mapCell.cellX
	final int cy = mapCell.cellY
	
	final replacements = needsReplacementMapper.get(entity)
	
	final pendingWaterTiles = entity.addAndReturn( state.engine.createComponent( HasPendingWaterTiles ) )
	
	final cornerHeights = new int[2][2]
	for(TileCorner corner : TileCorner.values())
		cornerHeights[corner.offsetX][corner.offsetY] = Util.max( seaLevel, state.map.getCellAltitude(cx, cy, corner) )
	
	//
	// This cell is tagged as needing a replacement water-tile for one or more corners.
	// Go through this list and see if any given corner should have water, or should lose water.
	// Then start the search for water-tiles to find tile(s) for that corner.
	replacements.corners.each { corner ->
		
		final int vx = cx + corner.offsetX
		final int vy = cy + corner.offsetY
		
		final cornerFlavors = new EnumMap(TileCorner)
		cornerFlavors.put corner, []
		if(state.map.isValidVertex(vx,vy)) {
			final vertexEntity = state.map.getVertexEntity(vx,vy)
			if(hasWaterMapper.has(vertexEntity))
				cornerFlavors.get(corner) << 'water'
		}
		
		pendingWaterTiles.futures.put corner, submitResultTask( { ->
			tileset.getMinimalTilesFor cornerHeights, cornerFlavors
		} )
	}
	
	entity.remove NeedsReplacementWaterTiles
}

iteratingSystem 'waterTileProcessingSystem', Family.all(IsMapCell, HasPendingWaterTiles).get(), { entity, deltaTime ->
	final mapCell = isCellMapper.get(entity)
	final int cx = mapCell.cellX
	final int cy = mapCell.cellY
	
	final pendingTiles = hasPendingTilesMapper.get(entity)
	
	def anyFinished = false
	final finishedCorners = new EnumMap(TileCorner)
	pendingTiles.futures.forEach { c, f ->
		if(f.isDone()) {
			anyFinished = true
			finishedCorners.put c, f.get()
			println "Got tile-fitting result for $cx,$cy ($c) - ${finishedCorners.get(c)}"
		}
	}
	
	if(!anyFinished)
		return
	
	finishedCorners.forEach { c, _ -> pendingTiles.futures.remove c }
	if(pendingTiles.futures.isEmpty())
		entity.remove HasPendingWaterTiles
	
//	final mapCell = isCellMapper.get(entity)
//	final int cx = mapCell.cellX
//	final int cy = mapCell.cellY
	
	def waterTiles
	if(hasWaterTilesMapper.has(entity))
		waterTiles = hasWaterTilesMapper.get(entity)
	else
		waterTiles = entity.addAndReturn( state.engine.createComponent(HasWaterTiles) )
	
	finishedCorners.forEach {c, t ->
		waterTiles.tiles.put c, t
	}
	
	//
	// Did we just remove all water-tiles from this HasWaterTiles component?
	def allEmpty = true
	waterTiles.tiles.forEach { c, t ->
		if(t != null && !t.isEmpty())
			allEmpty = false
	}
	if(allEmpty)
		entity.remove HasWaterTiles
}

include 'renderer.groovy'