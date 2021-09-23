
terrainTileMapper = ComponentMapper.getFor(HasTerrainTile)
selectedMapper = ComponentMapper.getFor(IsSelected)

customRenderHook 'terrainSelection', { deltaTime, batch, shapeDrawer, renderingSupport ->
	
	//
	// Iterate across visible map-cells ...
	final visibleCells = renderingSupport.visibleMapCells
	
	final int minX = (int) visibleCells.x, minY = (int) visibleCells.y
	final int maxX = (int)( visibleCells.x + visibleCells.width )
	final int maxY = (int)( visibleCells.y + visibleCells.height )
	for(int x=minX; x<maxX; x++)
		for(int y=minY; y<maxY; y++) {
			
			if(!state.map.isValidCell(x,y))
				continue
			
			final entity = state.map.getEntity(x,y)
			if(!terrainTileMapper.has(entity))
				continue
			if(!selectedMapper.has(entity))
				continue
			
			final select = selectedMapper.get(entity)
			final vertices = renderingSupport.getCellVertices( x, y, null )
			
			def color = Color.WHITE
			if(select.status == IsSelected.Status.INVALID)
				color = Color.SCARLET
			else if(select.status == IsSelected.Status.WARNING)
				color = Color.YELLOW
			else if(select.status == IsSelected.Status.INFORMATION)
				color = Color.TEAL
			else if(select.status == IsSelected.Status.OTHER)
				color = Color.VIOLET
			
			shapeDrawer.line vertices[0], vertices[1], color
			shapeDrawer.line vertices[1], vertices[2], color
			shapeDrawer.line vertices[2], vertices[3], color
			shapeDrawer.line vertices[3], vertices[0], color
		}
	
} after 'map'