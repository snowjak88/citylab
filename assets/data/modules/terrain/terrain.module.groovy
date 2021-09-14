id = 'terrain'

//
// Ensure our I18N bundle is properly registered.
i18n.addBundle 'i18n/terrain'

title = i18n.get('title')
description = i18n.get('description')

dependsOn 'cursor-highlighters'

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
visualParameter {
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
// We need to declare a couple of components for this Module to work.
//
// IsTerrainTile: marks an entity as having terrain-tile(s) assigned to it.
// IsTerrainTile is populated by the terrain-tile-fitter (in "systems.groovy"), and
// is read by the terrain-cell-renderer ("renderers/terrainRenderer.groovy").
//
class IsTerrainTile implements Component, Poolable {
	
	List<Tile> tiles = []
	
	//
	// Poolable components require a "reset" method,
	// to return them to a blank state
	void reset() {
		tiles.clear()
	}
}

//
// NeedsReplacementTerrainTile: marks an entity as requiring its IsTerrainTile
// to be recomputed (because the map has changed somehow at that location).
// This is a "marker" component, with no fields.
//
class NeedsReplacementTerrainTile implements Component, Poolable {
	void reset() { }
}

//
// PendingTerrainTile: marks an entity as waiting on its terrain-tiles to
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

//
// The "terrain" renderer will automatically share every variable we've declared so far.
include 'renderers/terrainRenderer.groovy'

//
// This module declares its entity-processing systems in another file.
// That file is loaded and processed now.
//
include 'systems.groovy'

//
// We define all our terain-tools in this file ...
//
include 'ui/tools.groovy'