import javax.management.remote.rmi.RMIConnector.Util

//
// Here we define the terrain tools -- raise and lower, really
//

// Give this Module a group in the tool-list
buttonGroup 'terrain-tools', {
	title = i18n.get 'terrain-tools'
}




//
// Here's a helper: check a vertex's neighbors to ensure that its altitude-delta
// with any respective neighbor don't exceed a certain amount.
// If that delta is too large, then the neighbor's altitude is increased/decreased
// to bring it within range, and then this closure is executed against that neighbor.
//

constrainVertexDeltas = { vertexX, vertexY, maxDelta = 1 ->
	final int vx = vertexX, vy = vertexY
	if(!state.map.isValidVertex(vx, vy))
		return
	
	final thisAltitude = state.map.getVertexAltitude(vx, vy)
	for(def dx =-1; dx<=1; dx++)
		for (def dy=-1; dy<=1; dy++) {
			if (dx == 0 && dy == 0)
				continue
			final int nx = vx + dx, ny = vy + dy
			if (!state.map.isValidVertex(nx, ny))
				continue
			
			final neighborAltitude = state.map.getVertexAltitude(nx, ny)
			final delta = thisAltitude - neighborAltitude
			if((int) Math.abs(delta) > (int) maxDelta) {
				final constrainedDelta = Util.clamp(delta, -maxDelta, +maxDelta)
				final newAltitude = neighborAltitude + constrainedDelta
				
				state.map.setVertexAltitude(nx, ny, newAltitude)
				constrainVertexDeltas(nx, ny, maxDelta)
				invalidateNeighbors(nx, ny)
			}
		}
}

invalidateNeighbors = { cellX, cellY ->
	final int cx = cellX, cy = cellY
	
	for(def dx=-1; dx<=1; dx++)
		for(def dy=-1; dy<=1; dy++) {
			if(!state.map.isValidCell(cx+dx, cy+dy))
				continue
			state.map.getEntity(cx+dx, cy+dy).add state.engine.createComponent( IsMapCellRearranged )
		}
}

modifyVertexAltitude = { vertexX, vertexY, altitudeDelta ->
	final int vx = vertexX, vy = vertexY
	
	if(!state.map.isValidVertex(vx,vy))
		return
	
	final altitude = state.map.getVertexAltitude(vx, vy)
	final int newAltitude = Util.clamp(altitude + altitudeDelta, 0, 8)
	
	state.map.setVertexAltitude(vx, vy, newAltitude)
	constrainVertexDeltas(vx, vx)
	for(def corner : TileCorner.values())
		invalidateNeighbors(vx - corner.offsetX, vy - corner.offsetY)
}

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
	
	mapHover { cellX, cellY ->
		mapCellOutliner.active = true
		mapCellOutliner.cellX = cellX
		mapCellOutliner.cellY = cellY
	}
	
	mapClick Buttons.LEFT, { cellX, cellY ->
		
		def lowestAlt = 999
		def lowestCorners = []
		int cx = cellX, cy = cellY
		for(def corner in TileCorner.values()) {
			final int vx = cx + corner.offsetX, vy = cy + corner.offsetY
			
			if(!state.map.isValidVertex(vx, vy))
				continue
			
			final altitude = state.map.getVertexAltitude(vx, vy)
			
			if(altitude < lowestAlt) {
				lowestAlt = altitude
				lowestCorners.clear()
			}
			if(altitude <= lowestAlt)
				lowestCorners << corner
		}
		
		lowestCorners.each { modifyVertexAltitude cx + it.offsetX, cy + it.offsetY, +1 }
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
	
	mapHover { cellX, cellY ->
		mapCellOutliner.active = true
		mapCellOutliner.cellX = cellX
		mapCellOutliner.cellY = cellY
	}
	
	mapClick Buttons.LEFT, { cellX, cellY ->
		def highestAlt = -999
		def highestCorners = []
		int cx = cellX, cy = cellY
		for(def corner in TileCorner.values()) {
			final int vx = cx + corner.offsetX, vy = cy + corner.offsetY
			
			if(!state.map.isValidVertex(vx, vy))
				continue
			
			final altitude = state.map.getVertexAltitude(vx, vy)
			
			if(altitude > highestAlt) {
				highestAlt = altitude
				highestCorners.clear()
			}
			if(altitude >= highestAlt)
				highestCorners << corner
		}
		
		highestCorners.each { modifyVertexAltitude cx + it.offsetX, cy + it.offsetY, -1 }
	}
	
	inactive {
		mapCellOutliner.active = false
	}
}

