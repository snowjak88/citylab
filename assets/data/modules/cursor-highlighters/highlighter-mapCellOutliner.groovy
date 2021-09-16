//
//
//
oldCellX = -1i
oldCellY = -1i
vertex0x = 0f
vertex0y = 0f
vertex1x = 0f
vertex1y = 0f
vertex2x = 0f
vertex2y = 0f
vertex3x = 0f
vertex3y = 0f

customRenderHook 'tool-mapCellOutliner', { delta, batch, shapeDrawer, renderingSupport ->
	if(!mapCellOutliner.active)
		return
	if( !renderingSupport.isCellVisible(mapCellOutliner.cellX, mapCellOutliner.cellY) )
		return
	
	if(mapCellOutliner.refresh || oldCellX != mapCellOutliner.cellX || oldCellY != mapCellOutliner.cellY) {
		final vertices = renderingSupport.getCellVertices( mapCellOutliner.cellX, mapCellOutliner.cellY, null )
		vertex0x = vertices[0].x
		vertex0y = vertices[0].y
		vertex1x = vertices[1].x
		vertex1y = vertices[1].y
		vertex2x = vertices[2].x
		vertex2y = vertices[2].y
		vertex3x = vertices[3].x
		vertex3y = vertices[3].y
		oldCellX = mapCellOutliner.cellX
		oldCellY = mapCellOutliner.cellY
		mapCellOutliner.refresh = false
	}
	
	shapeDrawer.line vertex0x, vertex0y, vertex1x, vertex1y, (mapCellOutliner.color ?: Color.WHITE )
	shapeDrawer.line vertex1x, vertex1y, vertex2x, vertex2y, (mapCellOutliner.color ?: Color.WHITE )
	shapeDrawer.line vertex2x, vertex2y, vertex3x, vertex3y, (mapCellOutliner.color ?: Color.WHITE )
	shapeDrawer.line vertex3x, vertex3y, vertex0x, vertex0y, (mapCellOutliner.color ?: Color.WHITE )
} after 'map'

//
//
//