id = 'roads'

dependsOn 'terrain'
dependsOn 'network'

i18n.addBundle 'i18n'

title = i18n.get('title')
description = i18n.get('description')

tilesetName = preferences.getString('tileset-name', 'default')
dependsOn tilesetName, TileSet

tileset = assets.getByID tilesetName, TileSet

visualParameter 'road-tileset', {
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

class NeedsReplacementRoadTile implements Component, Poolable {
	void reset() { }
}

eventComponent RoadCellUpdated

isCellMapper = ComponentMapper.getFor(IsMapCell)
isCellNonBuildableMapper = ComponentMapper.getFor(IsNonBuildableCell)

isNetworkNodeMapper = ComponentMapper.getFor(IsNetworkNode)
hasRoadMapper = ComponentMapper.getFor(HasRoad)

//
//
//

//
// Here's a useful helper-function:
// Can the given cell validly host a road-tile?
validRoadTiles = [
	[[0, 0], [0, 0]],
	[[1, 1], [0, 0]],
	[[0, 1], [0, 1]],
	[[0, 0], [1, 1]],
	[[1, 0], [1, 0]]
]
isValidRoadCell = { int cx, int cy ->
	
	if(!state.map.isValidCell(cx,cy))
		return false
	
	final entity = state.map.getEntity(cx,cy)
	
	if(isCellNonBuildableMapper.has(entity))
		return false
	
	//
	// A valid road-cell is either flat, or simply sloped -- i.e., there
	// are always 2 consecutive vertices at the same altitude
	
	//
	// "Normalize" the 4 vertices' altitudes
	final alt = new int[2][2]
	for(def corner : TileCorner)
		alt[corner.offsetX][corner.offsetY] = state.map.getCellAltitude(cx, cy, corner)
	def minAltitude = 99999
	for(def corner : TileCorner)
		minAltitude = Util.min( minAltitude, alt[corner.offsetX][corner.offsetY] )
	for(def corner : TileCorner)
		alt[corner.offsetX][corner.offsetY] -= minAltitude
	
	validRoadTiles.any {
		
		it[0][0] == alt[0][0] &&
				it[1][0] == alt[1][0] &&
				it[0][1] == alt[0][1] &&
				it[1][1] == alt[1][1]
	}
}

isValidRoadConnection = { int fromX, int fromY, int toX, int toY ->
	
	if(!isValidRoadCell(fromX, fromY))
		return false
	
	if(fromX == toX && fromY == toY)
		return true
	
	if(!isValidRoadCell(toX, toY))
		return false
	
	final edge = TileEdge.fromDelta(toX - fromX, toY - fromY)
	if(!edge)
		return false
	
	final alt1 = state.map.getCellAltitude(fromX, fromY, edge.corners[0])
	final alt2 = state.map.getCellAltitude(fromX, fromY, edge.corners[1])
	
	(alt1 == alt2)
}

//
//
//

include 'systems.groovy'
include 'tools/tools.groovy'