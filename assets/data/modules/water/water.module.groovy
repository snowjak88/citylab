id = 'water'

dependsOn 'terrain'

i18n.addBundle 'i18n'

title = i18n.get('title')
description = i18n.get('description')


tilesetName = preferences.getString('tileset-name', 'default')
dependsOn tilesetName, TileSet

tileset = assets.getByID tilesetName, TileSet

visualParameter {
	title = i18n.get('parameter-tileset')
	type = select {
		values = { assets.getAllByType(TileSet) }
		toString = { ts -> ts.title }
	}
	value = tileset
	onSet = { v ->
		tileset = v
		preferences.putString 'tileset-name', v.id
	}
}

//

seaLevel = preferences.getInteger('seaLevel', 0)

visualParameter {
	title = i18n.get('parameter-sealevel')
	type = intSpinner {
		min = 0
		max = 3
		step = 1
	}
	value = seaLevel
	onSet = { v ->
		seaLevel = v
		preferences.putInteger 'sealevel', v
	}
}

//
// Water-propagation is governed by a few basic mechanics:
//
// * Map-vertices can have a certain amount of water in them.
//   A map-vertex can be fractionally-full of water.
//
// * Some vertices are "flooded" -- that is, they don't participate in
//   flow-checks. Probably, the only vertices in this category are
//   those at or below sea-level and connected to the map-edge "infinite" vertices.
//
// * Map-vertices that have water will get rendered as such.
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
//

class HasWater implements Component, Poolable {
	float level
	void reset() {
		level = 0
	}
}

class IsFlooded implements Component, Poolable {
	void reset() { }
}
class IsFloodable implements Component, Poolable {
	void reset() { }
}

class NeedsReplacementWaterTiles implements Component, Poolable {
	void reset() { }
}
class HasPendingWaterTiles implements Component, Poolable {
	ListenableFuture<List<Tile>> future = null
	final EnumMap<TileCorner,Color> tints = new EnumMap(TileCorner)
	void reset() {
		future = null
		tints.clear()
	}
}
class HasWaterTiles implements Component, Poolable {
	final List<Tile> tiles = []
	final EnumMap<TileCorner,Color> tints = new EnumMap(TileCorner)
	void reset() {
		tiles.clear()
		tints.clear()
	}
}

isCellMapper = ComponentMapper.getFor(IsMapCell)
isVertexMapper = ComponentMapper.getFor(IsMapVertex)

hasWaterMapper = ComponentMapper.getFor(HasWater)
isFloodedMapper = ComponentMapper.getFor(IsFlooded)
isFloodableMapper = ComponentMapper.getFor(IsFloodable)

needsReplacementMapper = ComponentMapper.getFor(NeedsReplacementWaterTiles)
hasPendingTilesMapper = ComponentMapper.getFor(HasPendingWaterTiles)
hasWaterTilesMapper = ComponentMapper.getFor(HasWaterTiles)

//
//
// This holds the set of all vertices that are flooded
allFloodedVertices = new LinkedHashSet<>()

onActivate {
	->
	
	allFloodedVertices.clear()
	
	//
	// ensure that all edge-vertices at or below sea-level are marked as water-sources
	final maxX = state.map.width
	final maxY = state.map.height
	
	for(def x=0; x<=maxX; x++)
		for(def y=0; y<=maxY; y++) {
			if(state.map.getVertexAltitude(x,y) <= seaLevel) {
				final isEdge = (x == 0 || y == 0 || x == maxX || y == maxY)
				
				if(isEdge) {
					final entity = state.map.getVertexEntity(x,y)
					
					entity.add state.engine.createComponent(IsFloodable)
					entity.add state.engine.createComponent(IsFlooded)
					
					final water = entity.addAndReturn( state.engine.createComponent(HasWater) )
					water.level = 1
				}
			}
		}
}

//
// Deltas for neighboring vertices (because we don't
// allow diagonal connections between vertices).
final neighbors = [
	[-1, 0],
	[+1, 0],
	[0, -1],
	[0, +1]
]

//
// Keep the "allFloodedVertices" set up-to-date.
listeningSystem 'waterFloodedVertexListeningSystem', Family.all(IsFlooded).get(), { entity, deltaTime ->
	allFloodedVertices << entity
}, { entity, deltaTime ->
	allFloodedVertices.remove entity
}

//
// Check every Floodable entity to see if it can flood into neighboring vertices
intervalIteratingSystem 'waterFloodablePropagationSystem', Family.all(IsMapVertex, IsFloodable, HasWater).get(), 0.25, { entity, deltaTime ->
	
	final thisVertex = isVertexMapper.get(entity)
	final int vx = thisVertex.vertexX
	final int vy = thisVertex.vertexY
	
	final thisAltitude = state.map.getVertexAltitude(vx,vy)
	
	final thisWater = hasWaterMapper.get(entity)
	
	final validFloodTargets = []
	for(def neighbor : neighbors) {
		def (int dx, int dy) = neighbor
		final int nx = vx + dx, ny = vy + dy
		if(!state.map.isValidVertex(nx,ny))
			continue
		
		final neighborEntity = state.map.getVertexEntity(nx,ny)
		
		if(isFloodedMapper.has(neighborEntity))
			continue
		if(isFloodableMapper.has(neighborEntity))
			continue
		
		final neighborAltitude = state.map.getVertexAltitude(nx,ny)
		
		def isValidFloodTarget = false
		if(neighborAltitude <= thisAltitude)
			isValidFloodTarget = true
		else if(neighborAltitude <= seaLevel)
			isValidFloodTarget = true
		
		if(isValidFloodTarget)
			validFloodTargets << neighborEntity
	}
	
	//
	// If we've flooded everywhere we can, then this vertex should not be checked
	// anymore for flooding-from.
	if(validFloodTargets.isEmpty()) {
		entity.remove IsFloodable
		return
	}
	
	//
	// Pick a flood-target at random. Mark it as both Flooded and Floodable.
	
	final floodTargetIndex = state.rnd.nextInt( validFloodTargets.size() )
	final floodEntity = validFloodTargets[ floodTargetIndex ]
	floodEntity.add state.engine.createComponent(IsFlooded)
	floodEntity.add state.engine.createComponent(IsFloodable)
	final floodWater = floodEntity.addAndReturn( state.engine.createComponent( HasWater ) )
	floodWater.level = thisWater.level
}

//
// Whenever a map-vertex is marked as "rearranged", we need to ensure our water responds appropriately.
listeningSystem 'waterMapCellRearrangementSystem', Family.all(IsMapVertex, IsMapVertexRearranged).one(IsFlooded).get(), { entity, deltaTime ->
	
	final thisVertex = isVertexMapper.get(entity)
	final int vx = thisVertex.vertexX
	final int vy = thisVertex.vertexY
	
	//
	// If a Flooded vertex is rearranged, then:
	//
	//   1) that vertex loses its Flooded status and its HasWater
	//   2) its surrounding Flooded vertices become Floodable
	//
	if(isFloodedMapper.has(entity)) {
		
		entity.remove IsFlooded
		entity.remove HasWater
		
		for(def neighbor : neighbors) {
			def (int dx, int dy) = neighbor
			final int nx = vx + dx, ny = vy + dy
			if(!state.map.isValidVertex(nx,ny))
				continue
			
			final neighborEntity = state.map.getVertexEntity(nx,ny)
			
			if(isFloodedMapper.has(neighborEntity))
				neighborEntity.add state.engine.createComponent(IsFloodable)
		}
		
	}
	
}, {entity, deltaTime ->
	
}

//
// Whenever a vertex is tagged as HasWater, ensure its surrounding cells are marked as NeedsReplacementWaterTiles
setSurroundingTilesForReplacement = { entity, deltaTime ->
	final thisVertex = isVertexMapper.get(entity)
	final int vx = thisVertex.vertexX
	final int vy = thisVertex.vertexY
	
	for(def corner : TileCorner.values()) {
		final int cx = vx - corner.offsetX
		final int cy = vy - corner.offsetY
		if(!state.map.isValidCell(cx,cy))
			continue
		
		final cellEntity = state.map.getEntity(cx,cy)
		
		if(!needsReplacementMapper.has(cellEntity))
			cellEntity.add state.engine.createComponent( NeedsReplacementWaterTiles )
	}
}
listeningSystem 'waterNewHasWaterListeningSystem', Family.all(IsMapVertex, HasWater, IsFlooded).get(), setSurroundingTilesForReplacement, setSurroundingTilesForReplacement



windowIteratingSystem 'waterTileFittingSchedulingSystem', Family.all(IsMapCell, NeedsReplacementWaterTiles).exclude(HasPendingWaterTiles).get(), 8, { entity, deltaTime ->
	final mapCell = isCellMapper.get(entity)
	final int cx = mapCell.cellX
	final int cy = mapCell.cellY
	
	final replacements = needsReplacementMapper.get(entity)
	
	final pendingWaterTiles = entity.addAndReturn( state.engine.createComponent( HasPendingWaterTiles ) )
	
	final int[][] heights = new int[2][2]
	final predicates = []
	for(TileCorner corner : TileCorner.values()) {
		heights[corner.offsetX][corner.offsetY] = Util.max( seaLevel, state.map.getCellAltitude(cx, cy, corner) )
		
		final int vx = cx + corner.offsetX
		final int vy = cy + corner.offsetY
		
		if(state.map.isValidVertex(vx,vy)) {
			final vertexEntity = state.map.getVertexEntity(vx,vy)
			if(hasWaterMapper.has(vertexEntity)) {
				final c = corner
				predicates << { t -> t.ext?.water?.contains(c) }
				
				final hasWater = hasWaterMapper.get(vertexEntity)
				pendingWaterTiles.tints[corner] = new Color(1, 1, 1, Util.clamp(hasWater.level, 0, 0.8))
			}
		}
		
	}
	
	pendingWaterTiles.future = submitResultTask {
		->
		tileset.getMinimalTilesFor heights, predicates, true
	}
	
	entity.remove NeedsReplacementWaterTiles
}

windowIteratingSystem 'waterTileProcessingSystem', Family.all(IsMapCell, HasPendingWaterTiles).get(), 8, { entity, deltaTime ->
	final mapCell = isCellMapper.get(entity)
	final int cx = mapCell.cellX
	final int cy = mapCell.cellY
	
	final pendingTiles = hasPendingTilesMapper.get(entity)
	
	if(!pendingTiles.future.isDone())
		return
	
	def waterTiles
	if(hasWaterTilesMapper.has(entity))
		waterTiles = hasWaterTilesMapper.get(entity)
	else
		waterTiles = entity.addAndReturn( state.engine.createComponent(HasWaterTiles) )
	
	waterTiles.tiles.clear()
	waterTiles.tints.clear()
	
	final newTiles = pendingTiles.future.get()
	if(newTiles) {
		waterTiles.tiles.addAll newTiles
		waterTiles.tints.putAll pendingTiles.tints
	}
	
	if(waterTiles.tiles.isEmpty())
		entity.remove HasWaterTiles
	
	entity.remove HasPendingWaterTiles
}

include 'renderer.groovy'