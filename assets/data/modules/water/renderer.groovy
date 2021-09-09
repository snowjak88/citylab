cellRenderHook 'waterRender', { delta, cellX, cellY, support ->
	final entity = state.map.getEntity(cellX, cellY)
	if(entity)
		if(hasWaterTilesMapper.has(entity)) {
			final tiles = hasWaterTilesMapper.get(entity).tiles
			
			for(def corner : TileCorner.values()) {
				
				//
				// The water-tiles at the given corner/vertex need to be
				// tinted using the water-depth at that vertex.
				//
				// Accordingly, we need the HasWater component for that
				// vertex-entity.
				//
				final int vx = cellX + corner.offsetX
				final int vy = cellY + corner.offsetY
				
				final vertexEntity = state.map.getVertexEntity(vx,vy)
				if(!hasWaterMapper.has(vertexEntity))
					continue
				final hasWater = hasWaterMapper.get(vertexEntity)
				
				//
				// The tint is simply WHITE, made more-or-less transparent.
				final tileTint = new Color(1, 1, 1, hasWater.level).clamp()
				
				for(def tile in tiles[corner]) {
					//
					// Do we need to override this tile's rendered altitude?
					// If it'd be less than sea-level, then YES
					//
					final renderedAltitude = Util.max( seaLevel, state.map.getCellAltitude(cellX, cellY, tile.base) )
					support.renderTile cellX, cellY, tile, tileTint, renderedAltitude
				}
			}
		}
} after 'terrainRender'