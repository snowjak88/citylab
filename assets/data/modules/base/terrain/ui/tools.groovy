//
// Here we define the terrain tools -- raise and lower, really
//

// Give this Module a group in the tool-list
buttonGroup 'terrain-tools', {
	title = i18n.get 'terrain-tools'
}

//
// Ensure that the tools we're about to define are available
// in the default map-mode
//
mapModes['default'].tools << 'terrainRaise'
mapModes['default'].tools << 'terrainLevel'
mapModes['default'].tools << 'terrainLower'


minTerrainHeight = preferences.getInteger('min-terrain-height', 0)
maxTerrainHeight = preferences.getInteger('max-terrain-height', 10)


//
// Here's a helper: check a vertex's neighbors to ensure that its altitude-delta
// with any respective neighbor don't exceed a certain amount.
// If that delta is too large, then the neighbor's altitude is increased/decreased
// to bring it within range, and then this closure is executed against that neighbor.
//

constrainVertexDeltas = { int vertexX, vertexY, int maxDelta = 1 ->
	final int vx = vertexX, vy = vertexY
	if(!state.map.isValidVertex(vx, vy))
		return
	
	final thisAltitude = state.map.getVertexAltitude(vx, vy)
	for(def dx =-1; dx<=1; dx++)
		for (def dy=-1; dy<=1; dy++) {
			
			final int nx = vx + dx, ny = vy + dy
			if (!state.map.isValidVertex(nx, ny))
				continue
			
			final neighborAltitude = state.map.getVertexAltitude(nx, ny)
			final delta = thisAltitude - neighborAltitude
			if(Math.abs(delta) > maxDelta) {
				final constrainedDelta = Util.clamp(delta, -maxDelta, +maxDelta)
				final newAltitude = thisAltitude - constrainedDelta
				
				state.map.setVertexAltitude(nx, ny, newAltitude)
				constrainVertexDeltas(nx, ny, maxDelta)
				invalidateNeighbors(nx, ny)
			}
		}
}

cellChangedMapper = ComponentMapper.getFor(CellHeightChanged)
vertexChangedMapper = ComponentMapper.getFor(VertexHeightChanged)

invalidateNeighbors = { int vertexX, int vertexY ->
	for(def corner : TileCorner) {
		final cx = vertexX - corner.offsetX
		final cy = vertexY - corner.offsetY
		
		if(state.map.isValidCell(cx, cy)) {
			state.map.getEntity(cx,cy)?.add( state.engine.createComponent( CellHeightChanged ) )
		}
	}
	
	if(state.map.isValidVertex(vertexX, vertexY))
		state.map.getVertexEntity(vertexX, vertexY)?.add state.engine.createComponent( VertexHeightChanged )
}

modifyVertexAltitude = { int vertexX, int vertexY, desiredAltitude ->
	final vx = vertexX, vy = vertexY
	
	if(!state.map.isValidVertex(vx,vy))
		return
	
	final altitude = state.map.getVertexAltitude(vx, vy)
	final int newAltitude = Util.clamp(desiredAltitude, minTerrainHeight, maxTerrainHeight)
	
	state.map.setVertexAltitude(vx, vy, newAltitude)
	
	invalidateNeighbors(vx, vy)
}

//
// Define a tool

hoverX = -1
hoverY = -1
hoverEntity = null

updateHover = { cellX, cellY ->
	final int cx = cellX, cy = cellY
	
	if(!state.map.isValidCell(cx,cy)) {
		hoverEntity?.remove IsSelected
		hoverEntity = null
	} else
		if(hoverX != cx || hoverY != cy) {
			hoverEntity?.remove IsSelected
			hoverEntity = state.map.getEntity(cx,cy)
			hoverEntity.add state.engine.createComponent(IsSelected)
		}
	
	hoverX = cx
	hoverY = cy
}

tool 'terrainRaise', {
	
	title = i18n.get 'terrain-tools-raise'
	
	button {
		buttonUp = 'terrain_raise_button.png'
		buttonDown = 'terrain_raise_button.png'
		group = 'terrain-tools'
	}
	
	key {
		keys = 'Shift+R'
	}
	
	activeRaise = false
	raiseAlt = 0
	raiseCell = { cellX, cellY ->
		if(!activeRaise)
			return
		
		def lowestAlt = 999
		final lowestCorners = []
		int cx = cellX, cy = cellY
		for(def corner in TileCorner) {
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
		
		if(raiseAlt == 999)
			raiseAlt = lowestAlt + 1
		
		lowestCorners.each { modifyVertexAltitude cx + it.offsetX, cy + it.offsetY, raiseAlt }
		lowestCorners.each { constrainVertexDeltas cx + it.offsetX, cy + it.offsetY }
	}
	startRaise = { cellX, cellY ->
		raiseAlt = 999
		activeRaise = true
		raiseCell cellX, cellY
	}
	
	mapHover updateHover
	
	mapClick Buttons.LEFT, startRaise
	mapDragStart Buttons.LEFT, startRaise
	mapDragUpdate Buttons.LEFT, raiseCell
	mapDragEnd Buttons.LEFT, { cellX, cellY -> activeRaise = false }
	
	inactive {
		hoverEntity?.remove IsSelected
		hoverEntity = null
	}
}

tool 'terrainLevel', {
	
	title = i18n.get 'terrain-tools-level'
	
	button {
		buttonUp = 'terrain_level_button.png'
		buttonDown = 'terrain_level_button.png'
		group = 'terrain-tools'
	}
	
	activeLevel = false
	levelAlt = 999
	levelCell = { cellX, cellY ->
		if(!activeLevel)
			return
		
		final adjustCorners = []
		int cx = cellX, cy = cellY
		for(def corner in TileCorner) {
			final int vx = cx + corner.offsetX, vy = cy + corner.offsetY
			
			if(!state.map.isValidVertex(vx, vy))
				continue
			
			final altitude = state.map.getVertexAltitude(vx, vy)
			
			if(levelAlt == 999)
				levelAlt = altitude
			
			if(altitude != levelAlt)
				adjustCorners << corner
		}
		
		adjustCorners.each { modifyVertexAltitude cx + it.offsetX, cy + it.offsetY, levelAlt }
		adjustCorners.each { constrainVertexDeltas cx + it.offsetX, cy + it.offsetY }
	}
	startLevel = { cellX, cellY ->
		levelAlt = 999
		activeLevel = true
		levelCell cellX, cellY
	}
	
	mapHover updateHover
	
	mapDragStart Buttons.LEFT, startLevel
	mapDragUpdate Buttons.LEFT, levelCell
	mapDragEnd Buttons.LEFT, { cellX, cellY -> activeRaise = false }
	
	inactive {
		hoverEntity?.remove IsSelected
		hoverEntity = null
	}
}

tool 'terrainLower', {
	
	title = i18n.get 'terrain-tools-lower'
	
	button {
		buttonUp = 'terrain_lower_button.png'
		buttonDown = 'terrain_lower_button.png'
		group = 'terrain-tools'
	}
	
	key {
		keys = 'Shift+F'
	}
	
	mapHover updateHover
	
	activeLower = false
	lowerAlt = 0
	lowerCell = { cellX, cellY ->
		if(!activeLower)
			return
		
		def highestAlt = -999
		final highestCorners = []
		int cx = cellX, cy = cellY
		for(def corner in TileCorner) {
			final int vx = cx + corner.offsetX, vy = cy + corner.offsetY
			
			if(!state.map.isValidVertex(vx, vy))
				continue
			
			final altitude = state.map.getVertexAltitude(vx, vy)
			
			if(altitude > highestAlt) {
				highestAlt = altitude
				highestCorners.clear()
			}
			if(altitude <= highestAlt)
				highestCorners << corner
		}
		
		if(lowerAlt == -999)
			lowerAlt = highestAlt - 1
		
		highestCorners.each { modifyVertexAltitude cx + it.offsetX, cy + it.offsetY, lowerAlt }
		highestCorners.each { constrainVertexDeltas cx + it.offsetX, cy + it.offsetY }
	}
	startLower = { cellX, cellY ->
		lowerAlt = -999
		activeLower = true
		lowerCell cellX, cellY
	}
	
	mapClick Buttons.LEFT, startLower
	mapDragStart Buttons.LEFT, startLower
	mapDragUpdate Buttons.LEFT, lowerCell
	mapDragEnd Buttons.LEFT, { cellX, cellY -> activeLower = false }
	
	inactive {
		hoverEntity?.remove IsSelected
		hoverEntity = null
	}
}

