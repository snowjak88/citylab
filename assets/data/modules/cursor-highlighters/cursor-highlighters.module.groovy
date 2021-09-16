
id = 'cursor-highlighters'
description = 'Helper objects used by other modules'

//
// We publish the controls to a map-cell-outliner.
// This interface comes in the form of a provided object:
//
//  * mapCellOutliner.active (boolean)  -- is the map-cell-outliner active?
//  *                .cellX (int)       -- the X-coordinate of the map-cell to outline
//  *                .cellY (int)       -- the Y-coordinate ...
//  *                .refresh (boolean) -- force a re-calculation of the outliner's vertices
//  *                .color (Color)     -- color of the map-cell outline (null = WHITE)
//
//
class MapCellOutliner {
	boolean active, refresh
	int cellX, cellY
	Color color
	def String toString() {
		"[active: $active, refresh: $refresh, cellX: $cellX, cellY: $cellY, color: $color]"
	}
}
mapCellOutliner = [
	active: false,
	cellX: 0,
	cellY: 0,
	refresh: false,
	color: null
] as MapCellOutliner

//
// We'll make this interface-object available to other Modules under the name "mapCellOutliner"
//
provides mapCellOutliner named 'mapCellOutliner'

//
// Notice that our script-name *must* be distinct from the [mapCellOutliner] variable-name.
include 'highlighter-mapCellOutliner.groovy'

//
// We can extend that map-cell-outliner to an outliner for multiple cells simultaneously.
//
//  * mapCellListOutliner.active  (boolean) -- is the outliner active?
//  *                    .cellX   (List)    -- X-coordinates to outline
//  *                    .cellY   (List)    -- Y-coordinates to outline
//  *                    .refresh (boolean) -- force a re-calculation of the vertices for each cell
//                                             in the list instead of relying on the internal vertex-cache
//  *                    .color   (Color)   -- color of the map-cell outlines (null = WHITE)
//
class MapCellListOutliner {
	boolean active, refresh
	List cellX, cellY
	Color color
	def String toString() {
		"[active: $active, refresh: $refresh, cellX: $cellX, cellY: $cellY, color: $color]"
	}
}
mapCellListOutliner = [
	active: false,
	cellX: [],
	cellY: [],
	refresh: false,
	color: null
] as MapCellListOutliner

provides mapCellListOutliner named 'mapCellListOutliner'

include 'highlighter-mapCellListOutliner.groovy'