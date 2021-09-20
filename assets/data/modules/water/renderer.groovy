cellRenderHook 'waterRender', { delta, cellX, cellY, support ->
	final entity = state.map.getEntity(cellX, cellY)
	if(entity)
		if(hasWaterTileMapper.has(entity)) {
			final waterTile = hasWaterTileMapper.get(entity)
			
			if(!waterTile.tile)
				return
			
			final altitudeOverride = Util.max( sealevel, state.map.getCellAltitude(cellX, cellY, waterTile.tile.base) )
			support.renderTile cellX, cellY, waterTile.tile, altitudeOverride
		}
} after 'terrainRender'