cellRenderHook 'waterRender', { delta, cellX, cellY, support ->
	final entity = state.map.getEntity(cellX, cellY)
	if(entity)
		if(hasWaterTileMapper.has(entity)) {
			final waterTile = hasWaterTileMapper.get(entity)
			
			if(!waterTile.tile)
				return
			
			final tint = new Color( 1, 1, 1, Util.min( 0.8f, isWaterCellMapper.get(entity).levels[waterTile.tile.base] ) )
			final altitudeOverride = Util.max( sealevel, state.map.getCellAltitude(cellX, cellY, waterTile.tile.base) )
			support.renderTile cellX, cellY, waterTile.tile, tint, altitudeOverride
		}
} after 'terrainRender'