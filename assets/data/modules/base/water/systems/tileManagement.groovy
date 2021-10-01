
//
// When a cell is tagged as watery or not, tag it as NonBuildable or not.
//
listeningSystem 'waterTileNonBuildableSystem', Family.all(IsWateryCell).get(), { entity, deltaTime ->
	if(isCellNonBuildableMapper.has(entity))
		isCellNonBuildableMapper.get(entity).blockerIDs << 'water'
	else
		entity.addAndReturn( state.engine.createComponent( IsNonBuildableCell ) ).blockerIDs << 'water'
}, { entity, deltaTime ->
	isCellNonBuildableMapper.get(entity).blockerIDs.remove 'water'
}

//
// When an entity is tagged as needing a new water-tile, it needs to be updated.
//
iteratingSystem 'waterTileUpdatingSystem', Family.all(IsMapCell, NeedsNewWaterTile).exclude(UpdatedCellCharacteristics).get(), { entity, deltaTime ->
	
	final thisCell = isCellMapper.get(entity)
	final int cx = thisCell.cellX
	final int cy = thisCell.cellY
	
	final thisWater = isWaterCellMapper.get(entity)
	
	final updated = entity.addAndReturn( state.engine.createComponent(UpdatedCellCharacteristics) )
	updated.layerID = 'water'
	updated.tileset = tileset
	updated.searchSeparately = tileset.ext.water?.waterAsLayer
	
	for(def corner : TileCorner)
		updated.heights[corner.offsetX][corner.offsetY] = (sealevel <= state.map.getCellAltitude(cx,cy,corner)) ? sealevel : null
	
	updated.ext.water = []
	thisWater.levels.each { corner, level ->
		if(level > 0)
			updated.ext.water << corner
	}
	
	entity.remove NeedsNewWaterTile
}