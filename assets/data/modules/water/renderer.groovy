cellRenderHook 'waterRender', { delta, cellX, cellY, support ->
	final entity = state.map.getEntity(cellX, cellY)
	if(entity)
		if(hasWaterTilesMapper.has(entity)) {
			final waterTiles = hasWaterTilesMapper.get(entity)
			
			for(def tile in waterTiles.tiles) {
				//
				// Do we need to override this tile's rendered altitude?
				// If it'd be less than sea-level, then YES
				//
				final renderedAltitude = Util.max( seaLevel, state.map.getCellAltitude(cellX, cellY, tile.base) )
				support.renderTile cellX, cellY, tile, waterTiles.tints[tile.base], renderedAltitude
				
			}
		}
} after 'terrainRender'