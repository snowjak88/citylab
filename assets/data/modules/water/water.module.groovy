id = 'water'

i18n.addBundle 'i18n'

title = i18n.get 'title'
description = i18n.get 'description'

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

sealevel = preferences.getInteger('sealevel', 0)
visualParameter {
	title = i18n.get('parameter-sealevel')
	type = intSpinner {
		min = 0
		max = 4
		step = 1
	}
	value = sealevel
	onSet = { v ->
		sealevel = v
		preferences.putInteger 'sealevel', v
	}
}

//
//
//

class IsWateryVertex implements Component, Poolable {
	float level = 0
	void reset() {
		level = 0
	}
}

class IsWateryCell implements Component, Poolable {
	final EnumMap<TileCorner,Float> levels = new EnumMap(TileCorner)
	void reset() {
		levels.clear()
	}
}

class HasPendingWaterTile implements Component, Poolable {
	ListenableFuture<Tile> future = null
	void reset() {
		future = null
	}
}

class HasWaterTile implements Component, Poolable {
	void reset() { }
}

mapLayer 'water' after 'terrain'

//
//
//

//
// When this module is activated, ensure that all vertices at or below sea-level
// are marked as having water.
onActivate {
	->
	for(int vx=0; vx<=state.map.width; vx++)
		for(int vy=0; vy<=state.map.height; vy++)
			if(state.map.getVertexAltitude(vx,vy) <= sealevel) {
				
				final entity = state.map.getVertexEntity(vx,vy)
				
				final hasWater = entity.addAndReturn( state.engine.createComponent( IsWateryVertex ) )
				hasWater.level = 1.0
				
			}
}

isCellMapper = ComponentMapper.getFor(IsMapCell)
isCellRearrangedMapper = ComponentMapper.getFor(IsMapCellRearranged)
isCellNonBuildableMapper = ComponentMapper.getFor(IsNonBuildableCell)
hasLayersMapper = ComponentMapper.getFor(HasMapLayers)

isVertexMapper = ComponentMapper.getFor(IsMapVertex)
isVertexRearrangedMapper = ComponentMapper.getFor(IsMapVertexRearranged)

isWaterVertexMapper = ComponentMapper.getFor(IsWateryVertex)
isWaterCellMapper = ComponentMapper.getFor(IsWateryCell)
hasPendingWaterTileMapper = ComponentMapper.getFor(HasPendingWaterTile)

//
//
//

//
// When a vertex is un-/tagged as watery, check all its surrounding cells to see
// if they need to be tagged as watery or not.
checkWateryVertexCells = { Entity vertexEntity, int vx, int vy ->
	
	for(def corner : TileCorner) {
		final cx = vx - corner.offsetX
		final cy = vy - corner.offsetY
		
		if(!state.map.isValidCell(cx,cy))
			continue
		
		final cellEntity = state.map.getEntity(cx,cy)
		def cellWater
		if(!isWaterCellMapper.has(cellEntity))
			cellWater = cellEntity.addAndReturn( state.engine.createComponent( IsWateryCell ) )
		else
			cellWater = isWaterCellMapper.get(cellEntity)
		
		if(!isWaterVertexMapper.has(vertexEntity))
			cellWater.levels.remove corner
		else {
			final vertexWater = isWaterVertexMapper.get(vertexEntity)
			if(vertexWater.level <= 0)
				cellWater.levels.remove corner
			else
				cellWater.levels[corner] = vertexWater.level
		}
	}
}

listeningSystem 'wateryVertexCellUpdatingSystem', Family.all(IsMapVertex, IsWateryVertex).get(), { entity, deltaTime ->
	
	final thisVertex = isVertexMapper.get(entity)
	final vx = thisVertex.vertexX
	final vy = thisVertex.vertexY
	
	checkWateryVertexCells entity, (int) vx, (int) vy
	
}, { entity, deltaTime ->
	
	final thisVertex = isVertexMapper.get(entity)
	final vx = thisVertex.vertexX
	final vy = thisVertex.vertexY
	
	checkWateryVertexCells entity, (int) vx, (int) vy
}

//
//
//

include 'systems/tileManagement.groovy'