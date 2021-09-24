//
// Handles drawing an overlay of all connected road-nodes.
// This overlay is available under a specific map-mode.
//

renderHook 'road-network-renderer', { delta, batch, shapeDrawer, renderingSupport ->
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
			if(!hasRoadMapper.has(entity))
				continue
			final road = hasRoadMapper.get(entity)
				
			def vertices = renderingSupport.getCellVertices( x, y, null )
			final color = Color.YELLOW
			
			final float middleX0 = ( vertices[0].x + vertices[1].x + vertices[2].x + vertices[3].x ) / 4.0
			final float middleY0 = ( vertices[0].y + vertices[1].y + vertices[2].y + vertices[3].y ) / 4.0
			
			for(def edge : road.edges) {
				
				vertices = renderingSupport.getCellVertices( x + edge.dx, y + edge.dy, null )
				final float middleX1 = ( vertices[0].x + vertices[1].x + vertices[2].x + vertices[3].x ) / 4.0
				final float middleY1 = ( vertices[0].y + vertices[1].y + vertices[2].y + vertices[3].y ) / 4.0
				
				shapeDrawer.line middleX0, middleY0, middleX1, middleY1, color
				
			}
		}
}

mapMode 'road-network', {
	title = i18n.get('mapmode-roadnetwork')
	description = i18n.get('mapmode-roadnetwork-description')
	includes << 'default'
	renderingHooks << 'road-network-renderer'
}