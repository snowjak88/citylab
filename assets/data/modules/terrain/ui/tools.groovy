import java.awt.Color

//
// Here we define the terrain tools -- raise and lower, really
//

// Give this Module a group in the tool-list
buttonGroup 'terrain-tools', {
	title = 'Terrain Tools'
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

customRenderHook 'tool-mapCellOutliner', { delta, batch, shapeDrawer, renderingSupport ->
	if(!mapCellOutliner.active)
		return
	if( !renderingSupport.isCellVisible(mapCellOutliner.cellX, mapCellOutliner.cellY) )
		return
	
	final vertices = renderingSupport.getCellVertices( mapCellOutliner.cellX, mapCellOutliner.cellY, null )
	shapeDrawer.color = Color.WHITE
	shapeDrawer.line vertices[0], vertices[1]
	shapeDrawer.line vertices[1], vertices[2]
	shapeDrawer.line vertices[2], vertices[3]
	shapeDrawer.line vertices[3], vertices[0]
} after 'map'

//
//
//

//
// Define a tool
tool 'terrainRaise', {
	
	button 'terrain.raise', {
		title = 'Raise terrain'
		buttonUp = 'terrain_raise_button.png'
		buttonDown = 'terrain_raise_button.png'
		group = 'terrain-tools'
	}
	
	key 'terrain.raise', {
		title = 'Raise Terrain'
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
		title = 'Lower terrain'
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

