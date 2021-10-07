id = 'water'

dependsOn 'tile-fitting'

i18n.addBundle 'i18n'

title = i18n.get 'title'
description = i18n.get 'description'

tilesetName = preferences.getString('tileset-name', 'default')
dependsOn tilesetName, TileSet

tileset = assets.getByID tilesetName, TileSet

visualParameter 'water-tileset', {
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
visualParameter 'water-sealevel', {
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

class NeedsNewWaterTile implements Component, Poolable {
	void reset() { }
}

//
//
//

//
// When this module is activated, ensure that all relevant vertices at or below sea-level
// are marked as having water.
//
// Which vertices are "relevant" depends on a tile-set property:
//   tileset.ext.water.shoresOnFlatGround
//
// If this property is false, then the tileset is designed to show water as
// "rising" to partway up a slope. *All* vertices below sea-level should be marked as watery.
//
// If this property is true, then the tileset is designed to show water as
// being part of a level plain. Vertices on the border between "sea-level" and "not" must *not*
// be marked as watery.
//
onActivate {
	->
	for(int vx=0; vx<=state.map.width; vx++)
		for(int vy=0; vy<=state.map.height; vy++)
			if(state.map.getVertexAltitude(vx,vy) <= sealevel) {
				
				def isBorderVertex = false
				if(tileset.ext.water?.shoresOnFlatGround)
					//
					// test all neighboring vertices to see if this vertex counts as a "border" vertex
					for(int dx=-1; dx<=+1; dx++)
						for(int dy=-1; dy<=+1; dy++){
							final int nx = vx + dx
							final int ny = vy + dy
							if(!state.map.isValidVertex(nx,ny))
								continue
							
							if(state.map.getVertexAltitude(nx,ny) > sealevel) {
								isBorderVertex = true
								break
							}
						}
				
				if(isBorderVertex)
					continue
				
				final entity = state.map.getVertexEntity(vx,vy)
				
				final hasWater = entity.addAndReturn( state.engine.createComponent( IsWateryVertex ) )
				hasWater.level = 1.0
				
			}
}

//
//
//

isCellMapper = ComponentMapper.getFor(IsMapCell)
isCellRearrangedMapper = ComponentMapper.getFor(IsMapCellRearranged)
isCellNonBuildableMapper = ComponentMapper.getFor(IsNonBuildableCell)

isVertexMapper = ComponentMapper.getFor(IsMapVertex)
isVertexRearrangedMapper = ComponentMapper.getFor(IsMapVertexRearranged)

isWaterVertexMapper = ComponentMapper.getFor(IsWateryVertex)
isWaterCellMapper = ComponentMapper.getFor(IsWateryCell)
needsNewWaterMapper = ComponentMapper.getFor(NeedsNewWaterTile)

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
		
		cellEntity.add state.engine.createComponent( NeedsNewWaterTile )
	}
}

listeningSystem 'wateryVertexCellUpdatingSystem', Family.all(IsMapVertex, IsWateryVertex).get(), { entity, deltaTime ->
	
	final thisVertex = isVertexMapper.get(entity)
	final vx = thisVertex.vertexX
	final vy = thisVertex.vertexY
	
	checkWateryVertexCells entity, (int) vx, (int) vy
	
}, { entity, deltaTime ->
	
	if(isVertexMapper.has(entity)) {
		final thisVertex = isVertexMapper.get(entity)
		final vx = thisVertex.vertexX
		final vy = thisVertex.vertexY
		
		checkWateryVertexCells entity, (int) vx, (int) vy
	}
}

//
//
//

include 'systems/tileManagement.groovy'