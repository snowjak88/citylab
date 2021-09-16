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

include 'systems.groovy'
include 'tools.groovy'
include 'renderer.groovy'