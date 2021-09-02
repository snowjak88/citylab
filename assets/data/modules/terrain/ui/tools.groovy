//
// Here we define the terrain tools -- raise and lower, really
//

// Give this Module a group in the tool-list
buttonGroup 'terrain-tools', {
	title = i18n.get 'terrain-tools'
}

//
// We can publish the controls to a map-cell-outliner.
// This interface comes in the form of a provided object:
//
//  * mapCellOutliner.active (boolean) -- is the map-cell-outliner active?
//  * mapCellOutliner.cellX (int)      -- the X-coordinate of the map-cell to outline
//  * mapCellOutliner.cellY (int)      -- the Y-coordinate ...
//
//
class MapCellOutliner {
	boolean active
	int cellX, cellY
}
mapCellOutliner = [
	active: false,
	cellX: 0,
	cellY: 0
] as MapCellOutliner

provides mapCellOutliner named 'mapCellOutliner'

oldCellX = -1i
oldCellY = -1i
vertex0x = 0
vertex0y = 0
vertex1x = 0
vertex1y = 0
vertex2x = 0
vertex2y = 0
vertex3x = 0
vertex3y = 0

customRenderHook 'tool-mapCellOutliner', { delta, batch, shapeDrawer, renderingSupport ->
	if(!mapCellOutliner.active)
		return
	if( !renderingSupport.isCellVisible(mapCellOutliner.cellX, mapCellOutliner.cellY) )
		return
	
	if(oldCellX != mapCellOutliner.cellX || oldCellY != mapCellOutliner.cellY) {
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
	}
	
	shapeDrawer.line vertex0x, vertex0y, vertex1x, vertex1y, Color.WHITE
	shapeDrawer.line vertex1x, vertex1y, vertex2x, vertex2y, Color.WHITE
	shapeDrawer.line vertex2x, vertex2y, vertex3x, vertex3y, Color.WHITE
	shapeDrawer.line vertex3x, vertex3y, vertex0x, vertex0y, Color.WHITE
} after 'map'

//
//
//

//
// Define a tool
tool 'terrainRaise', {
	
	title = i18n.get 'terrain-tools-raise'
	
	button 'terrain.raise', {
		buttonUp = 'terrain_raise_button.png'
		buttonDown = 'terrain_raise_button.png'
		group = 'terrain-tools'
	}
	
	key 'terrain.raise', {
		keys = 'Shift+R'
	}
	
	active.mapHover { cellX, cellY ->
		mapCellOutliner.active = true
		mapCellOutliner.cellX = cellX
		mapCellOutliner.cellY = cellY
	}
	
	inactive {
		mapCellOutliner.active = false
	}
}

tool 'terrainLower', {
	
	button 'terrain.lower', {
		title = i18n.get 'terrain-tools-lower'
		buttonUp = 'terrain_lower_button.png'
		buttonDown = 'terrain_lower_button.png'
		group = 'terrain-tools'
	}
	
	active.mapHover { cellX, cellY ->
		mapCellOutliner.active = true
		mapCellOutliner.cellX = cellX
		mapCellOutliner.cellY = cellY
	}
	
	inactive {
		mapCellOutliner.active = false
	}
}

