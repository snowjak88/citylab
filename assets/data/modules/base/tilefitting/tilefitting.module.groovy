id = 'tile-fitting'

i18n.addBundle 'i18n'

title = i18n.get 'title'
description = i18n.get 'description'

//
//
//

class MapCellCharacteristics implements Component, Poolable {
	
	final Map<String,Expando> characteristics = [:]
	final Map<String,Integer> altitudeOverrides = [:]
	final Set<String> separateSearch = []
	final Map<String,TileSet> tilesets = [:]
	final Map<String,Integer[][]> heights = [:]
	
	void reset() {
		characteristics.clear()
		altitudeOverrides.clear()
		separateSearch.clear()
		tilesets.clear()
		heights.clear()
	}
}

class IsDirtyMapCellCharacteristics implements Component, Poolable {
	void reset() { }
}

class PendingCellTile {
	TileSet tileset
	boolean searchSeparately = false
	final Expando characteristics = new Expando()
	Integer altitudeOverride
	int[][] heights
}

class NeedsMapCellTiles implements Component, Poolable {
	final Set<PendingCellTile> pending = []
	void reset() {
		pending.clear()
	}
}

class PendingMapCellTiles implements Component, Poolable {
	ListenableFuture<List<HasMapCellTiles.MapCellTile>> future
	void reset() {
		future = null
	}
}

//
//
//

isCellMapper = ComponentMapper.getFor(IsMapCell)
mapCellCharacteristicsMapper = ComponentMapper.getFor(MapCellCharacteristics)
updatedCharacteristicsMapper = ComponentMapper.getFor(UpdatedCellCharacteristics)
dirtyCharacteristicsMapper = ComponentMapper.getFor(IsDirtyMapCellCharacteristics)
needsTilesMapper = ComponentMapper.getFor(NeedsMapCellTiles)
pendingTilesMapper = ComponentMapper.getFor(PendingMapCellTiles)
hasTilesMapper = ComponentMapper.getFor(HasMapCellTiles)

//
//
//

eventComponent UpdatedCellCharacteristics, { entity, deltaTime ->
	
	//
	// If the incoming UpdatedCellCharacteristics doesn't have all
	// the required fields, then ignore it.
	final updated = updatedCharacteristicsMapper.get(entity)
	if(!updated.layerID)
		return
	if(!updated.tileset)
		return
	if(!updated.heights)
		return
	if(updated.heights.length != 2)
		return
	if(updated.heights[0].length != 2)
		return
	if(updated.ext.properties.isEmpty())
		return
	
	//
	// First: update MapCellCharacteristics to match the incoming UpdatedCellCharacteristics
	//
	
	def mapCellCharacteristics = mapCellCharacteristicsMapper.get(entity)
	if(!mapCellCharacteristics)
		mapCellCharacteristics = entity.addAndReturn( state.engine.createComponent(MapCellCharacteristics) )
	
	if(!mapCellCharacteristics.characteristics[updated.layerID])
		mapCellCharacteristics.characteristics[updated.layerID] = new Expando()
	
	mapCellCharacteristics.characteristics[updated.layerID].properties.clear()
	mapCellCharacteristics.characteristics[updated.layerID].properties.putAll updated.ext.properties
	
	mapCellCharacteristics.heights[updated.layerID] = new Integer[2][2]
	mapCellCharacteristics.heights[updated.layerID][0][0] = updated.heights[0][0]
	mapCellCharacteristics.heights[updated.layerID][0][1] = updated.heights[0][1]
	mapCellCharacteristics.heights[updated.layerID][1][0] = updated.heights[1][0]
	mapCellCharacteristics.heights[updated.layerID][1][1] = updated.heights[1][1]
	
	mapCellCharacteristics.tilesets[updated.layerID] = updated.tileset
	
	mapCellCharacteristics.altitudeOverrides[updated.layerID] = updated.altitudeOverride
	
	if(updated.searchSeparately)
		mapCellCharacteristics.separateSearch << updated.layerID
	else
		mapCellCharacteristics.separateSearch.remove updated.layerID
	
	//
	// Mark this entity as "dirty"
	//
	if(!dirtyCharacteristicsMapper.has(entity))
		entity.add state.engine.createComponent(IsDirtyMapCellCharacteristics)
}

//
// Where we have a "dirty-characteristics" cell, build the tile-fitting request.
iteratingSystem 'dirtyMapCellCharacteristicsProcessingSystem', Family.all(IsMapCell, MapCellCharacteristics, IsDirtyMapCellCharacteristics).exclude(NeedsMapCellTiles).get(), { entity, deltaTime ->
	
	//
	// Iterate across every "layer-ID" we have, and aggregate characteristics across
	// every TileSet / altitudeOverride combination we find.
	//
	final needsTiles = entity.addAndReturn( state.engine.createComponent(NeedsMapCellTiles) )
	final mapCellCharacteristics = mapCellCharacteristicsMapper.get(entity)
	
	final thisCell = isCellMapper.get(entity)
	final int cx = thisCell.cellX
	final int cy = thisCell.cellY
	final int[][] normalHeights = new int[2][2]
	for(def corner : TileCorner)
		normalHeights[corner.offsetX][corner.offsetY] = state.map.getCellAltitude(cx, cy, corner)
	
	mapCellCharacteristics.characteristics.each { layerID, characteristics ->
		
		final tileset = mapCellCharacteristics.tilesets[layerID]
		final altitudeOverride = mapCellCharacteristics.altitudeOverrides[layerID]
		final heights = new int[2][2]
		final separateSearch = mapCellCharacteristics.separateSearch.contains layerID
		
		heights[0][0] = mapCellCharacteristics.heights[layerID][0][0] ?: normalHeights[0][0]
		heights[0][1] = mapCellCharacteristics.heights[layerID][0][1] ?: normalHeights[0][1]
		heights[1][0] = mapCellCharacteristics.heights[layerID][1][0] ?: normalHeights[1][0]
		heights[1][1] = mapCellCharacteristics.heights[layerID][1][1] ?: normalHeights[1][1]
		
		def pending = null
		if(!separateSearch)
			pending = needsTiles.pending.find { p ->
				p.tileset === tileset &&
						p.heights &&
						p.heights[0][0] == heights[0][0] &&
						p.heights[0][1] == heights[0][1] &&
						p.heights[1][0] == heights[1][0] &&
						p.heights[1][1] == heights[1][1]
			}
		if(!pending) {
			pending = [ tileset: tileset, altitudeOverride: altitudeOverride, heights: heights, searchSeparately: separateSearch ] as PendingCellTile
			needsTiles.pending << pending
		}
		pending.characteristics.properties.putAll characteristics.properties
		
	}
	
	entity.remove IsDirtyMapCellCharacteristics
}

//
// Submit any tile-fitting requests.
iteratingSystem 'mapCellFittingTaskSubmissionSystem', Family.all(NeedsMapCellTiles).exclude(PendingMapCellTiles).get(), { entity, deltaTime ->
	
	final needsTiles = needsTilesMapper.get(entity)
	
	final pendingCells = []
	pendingCells.addAll needsTiles.pending
	
	final pending = entity.addAndReturn( state.engine.createComponent(PendingMapCellTiles) )
	pending.future = submitResultTask {
		->
		final result = []
		pendingCells.each { p ->
			final tile = p.tileset.getTileFor p.heights, p.characteristics
			if(tile)
				result << ( [ tile: tile, altitudeOverride: p.altitudeOverride ] as HasMapCellTiles.MapCellTile )
		}
		
		result.sort { t1,t2 -> Integer.compare(t1.tile.zOrder, t2.tile.zOrder) }
		result
	}
	
	entity.remove NeedsMapCellTiles
	
}

//
// When a tile-fitting request is complete, ensure the HasMapCellTiles
// component is updated.
iteratingSystem 'mapCellFittingTaskCompletionSystem', Family.all(PendingMapCellTiles).get(), { entity, deltaTime ->
	
	final pending = pendingTilesMapper.get(entity)
	if(!pending.future.done)
		return
	
	def mapTiles = hasTilesMapper.get(entity)
	if(!mapTiles)
		mapTiles = entity.addAndReturn( state.engine.createComponent(HasMapCellTiles) )
	
	mapTiles.tiles.addAll pending.future.get()
	
	entity.remove PendingMapCellTiles
	
}