cellRenderHook 'waterRender', { delta, cellX, cellY, support ->
	final entity = state.map.getEntity(cellX, cellY)
	if(entity)
		if(hasWaterTilesMapper.has(entity)) {
			final waterTile = hasWaterTilesMapper.get(entity)
			
			for(def tile in waterTile.tiles) {
				final tint = new Color( 1, 1, 1, Util.min( 0.8f, isWaterCellMapper.get(entity).levels[tile.base] ) )
				final altitudeOverride = Util.max( sealevel, state.map.getCellAltitude(cellX, cellY, tile.base) )
				support.renderTile cellX, cellY, tile, tint, altitudeOverride
			}
		}
} after 'terrainRender'