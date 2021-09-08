//
// ComponentMappers make us faster at querying and retrieving Components from entities
terrainMapper = ComponentMapper.getFor(IsTerrainTile)

//
// Declare a cell-rendering hook into the map-rendering loop.
// This will be called every frame for every on-screen map-cell.
//
// Note that this hook as a name.
// Names must be unique for cell-rendering hooks. Hooks that are
// registered later will overwrite those registered earlier.
//
cellRenderHook 'terrainRender', { delta, cellX, cellY, support ->
	final entity = state.map.getEntity(cellX, cellY)
	if(entity)
		if(terrainMapper.has(entity))
			for(def tile in terrainMapper.get(entity).tiles)
				support.renderTile cellX, cellY, tile
}
