id = 'terrain'

//
// Ensure our I18N bundle is properly registered.
i18n.addBundle 'i18n/terrain'

title = i18n.get('title')
description = i18n.get('description')

//
// Get the configured tile-set name to use for the landscape,
// with a pre-programmed fallback
tilesetName = preferences.getString('tileset-name', 'default')

//
// Get that tileset from the tile-set service.
// Note that we need to mark that tile-set as explicitly depended-upon
// so it can be fully loaded before we need it.
//
dependsOn tilesetName, TileSet
tileset = assets.getByID tilesetName, TileSet

//
// Define a "visual parameter" -- i.e., a GUI element that can be
// displayed by the game-setup screen.
//
visualParameter 'terrain-tileset', {
	title = i18n.get('terrain-tileset')
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
// NeedsReplacementTerrainTile: marks an entity as requiring its terrain-tile
// to be recomputed (because the map has changed somehow at that location).
// This is a "marker" component, with no fields.
//
class NeedsReplacementTerrainTile implements Component, Poolable {
	void reset() { }
}

//
// PendingTerrainTile: marks an entity as waiting on its terrain-tile to
// be (re-)computed. Has a Future, giving us an endpoint for the background-task
// that's busy doing that terrain-tile-fitting.
//
class PendingTerrainTile implements Component, Poolable {
	ListenableFuture<?> future
	final int[][] heights = new int[2][2]
	void reset() {
		future = null
		heights[0][0] = 0
		heights[1][0] = 0
		heights[0][1] = 0
		heights[1][1] = 0
	}
}

// HasTerrainTile: marks an entity as having had its terrain-tile computed.
class HasTerrainTile implements Component, Poolable {
	void reset() { }
}

mapLayer 'terrain'

//
// This module declares its entity-processing systems in another file.
// That file is loaded and processed now.
//
include 'systems.groovy'

//
// We define all our terain-tools in this file ...
//
include 'ui/tools.groovy'

//
// This defines a custom-rendering-hook that draws a highlight
// around any terrain-tile that IsSelected
//
include 'highlight-renderer.groovy'