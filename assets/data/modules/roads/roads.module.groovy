id = 'roads'

dependsOn 'terrain'
dependsOn 'cursor-highlighters'

i18n.addBundle 'i18n'

title = i18n.get('title')
description = i18n.get('description')

tilesetName = preferences.getString('tileset-name', 'default')
dependsOn tilesetName, TileSet

tileset = assets.getByID tilesetName, TileSet

visualParameter {
	title = i18n.get('parameter-tileset')
	type = select {
		values = { assets.getAllByType(TileSet) }
		toString = { ts -> ts.title }
	}
	value = tileset
	onSet = { v ->
		tileset = v
		preferences.putString 'tileset-name', v.id
	}
}

//
//
//

class HasRoad implements Component, Poolable {
	Set<TileEdge> edges = []
	void reset() {
		edges.clear()
	}
}

class HasPendingRoadTile implements Component, Poolable {
	ListenableFuture<List<Tile>> future
	void reset() {
		future = null
	}
}

class HasRoadTile implements Component, Poolable {
	List<Tile> tiles = []
	void reset() {
		tiles.clear()
	}
}

isCellMapper = ComponentMapper.getFor(IsMapCell)
hasRoadMapper = ComponentMapper.getFor(HasRoad)
hasPendingRoadMapper = ComponentMapper.getFor(HasPendingRoadTile)
hasRoadTileMapper = ComponentMapper.getFor(HasRoadTile)

//
//
//

//
// Here's a useful helper-function:
// Can the given cell validly host a road-tile?
isValidRoadCell = { int cx, int cy ->
	
	if(!state.map.isValidCell(cx,cy))
		return false
	
	final entity = state.map.getEntity(cx,cy)
	
	//
	// A valid road-cell is either flat, or simply sloped -- i.e., there
	// are always 2 consecutive vertices at the same altitude
	
	def isFlat = true
	def altitude = state.map.getCellAltitude(cx, cy, TileCorner.TOP)
	def lastAltitude = altitude
	def consecutiveSameAltitudeCount = 0
	def lastConsecutiveSameAltitudeCount = 0
	
	for(def corner in [ TileCorner.RIGHT, TileCorner.BOTTOM, TileCorner.LEFT ]) {
		final thisAltitude = state.map.getCellAltitude(cx,cy,corner)
		
		if(thisAltitude != lastAltitude) {
			isFlat = false
			lastConsecutiveSameAltitudeCount = consecutiveSameAltitudeCount
			consecutiveSameAltitudeCount = 0
		}
		
		if(thisAltitude == lastAltitude)
			consecutiveSameAltitudeCount++
		
		lastAltitude = thisAltitude
	}
	
	( isFlat ) || ( lastConsecutiveSameAltitudeCount == 1 ) || ( consecutiveSameAltitudeCount == 1 )
}

//
//
//

include 'pathfinding.groovy'
include 'systems.groovy'
include 'tools.groovy'
include 'renderer.groovy'