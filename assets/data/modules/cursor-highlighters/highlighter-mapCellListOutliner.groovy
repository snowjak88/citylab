//
//
//

final verticesX = new float[4]
final verticesY = new float[4]

//
// Populate [verticesX] and [verticesY]
//
getVertices = { int cx, int cy, renderingSupport ->
	
	final vertices = renderingSupport.getCellVertices( cx, cy, null )
	
	verticesX[0] = vertices[0].x
	verticesY[0] = vertices[0].y
	verticesX[1] = vertices[1].x
	verticesY[1] = vertices[1].y
	verticesX[2] = vertices[2].x
	verticesY[2] = vertices[2].y
	verticesX[3] = vertices[3].x
	verticesY[3] = vertices[3].y
}

customRenderHook 'tool-mapCellListOutliner', { delta, batch, shapeDrawer, renderingSupport ->
	if(!mapCellListOutliner.active)
		return
	
	final maxIndex = Util.min( mapCellListOutliner.cellX.size(), mapCellListOutliner.cellY.size() )
	for(def i=0; i<maxIndex; i++) {
		final int cx = mapCellListOutliner.cellX[i]
		final int cy = mapCellListOutliner.cellY[i]
		
		if(!state.map.isValidCell(cx,cy))
			continue
		
		getVertices cx,cy, renderingSupport
		
		shapeDrawer.line verticesX[0], verticesY[0], verticesX[1], verticesY[1], (mapCellListOutliner.color ?: Color.WHITE )
		shapeDrawer.line verticesX[1], verticesY[1], verticesX[2], verticesY[2], (mapCellListOutliner.color ?: Color.WHITE )
		shapeDrawer.line verticesX[2], verticesY[2], verticesX[3], verticesY[3], (mapCellListOutliner.color ?: Color.WHITE )
		shapeDrawer.line verticesX[3], verticesY[3], verticesX[0], verticesY[0], (mapCellListOutliner.color ?: Color.WHITE )
	}
} after 'map'