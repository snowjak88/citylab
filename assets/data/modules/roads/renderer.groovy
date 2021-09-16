cellRenderHook 'roadRender', { delta, cellX, cellY, support ->
	final entity = state.map.getEntity(cellX, cellY)
	if(entity)
		if(hasRoadTileMapper.has(entity)) {
			final roadTile = hasRoadTileMapper.get(entity)
			
			for(def tile in roadTile.tiles)
				support.renderTile cellX, cellY, tile
		}
} after 'terrainRender'