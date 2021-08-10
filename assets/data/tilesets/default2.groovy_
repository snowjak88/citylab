
id = "default"
title = "Default tileset"
description = "Default world tileset. Uses assets created by Kenney Vleugels (www.kenney.nl)."

//
// The grid-dimensions for which this tileset was designed
//
// (Default: 32 / 16)
gridWidth = 132
gridHeight = 66

//
// Default width/height for all tiles. (Visible width/height, as distinct from grid width/height.)
//
// 0 = entire image (minus padding)
//
// (Default: 32 / 16)
width = 0
height = 0

//
// Drawing offset (along Y axis, from the tile's bottom edge)
// i.e., how far should the tile be offset so its visual "bottom" corner
// is aligned with the grid-cell's "bottom" corner
//
// Useful if your tiles contain some "subsurface" detail that you want
// to be positioned "below" the grid.
//
// (Default: 0)
surfaceOffset = 32

//
// Every unit of altitude should add/subtract these many pixels from this tile's Y-position.
altitudeOffset = 32

//
// All image-references will be made relative to the given sub-directory.
// This sub-directory must be in the same directory as this descriptor.
//
// (Default: no default)
folder = "default/landscape"

//
// Default file-name for all tiles
//
// (Default: no default)
// filename = "default.png"

//
// Starting x/y within the image-file for each tile
//
// (Default: 0 / 0)
x = 0
y = 0

//
// Each tile needs to be drawn with a vertical offset depending on its altitude.
// The tile-renderer should consult this corner of each tile to determine which
// of that tile's vertices should contribute its altitude to that vertical offset.
//
// (Default: TOP)
base = TOP

//
// Controls whether the next tile will be "decoration" -- i.e., is transparent
// and intended to be layered on top of other tiles to add effects.
//
// (Default: false)
decoration = false

//
// Each tile's image has so much padding (empty space surrounding
// the "meat" of the tile, and which should be ignored)
//
// (Default: 0)
padding = 0

//
// "Auto-advance" x/y for every subsequent tile
//
// If your tile-images are all part of a single tile-sheet, this can be
// useful.
//
// Because [filename] is not loaded right when the script is executed,
// you need to supply the image dimensions here
//
// Once set up, the "cursor" will iterate to the next tile-slot (i.e., update x/y
// by width/height) with every call to tile(). It's expected that we iterate across
// the tile-sheet row by row, from left to right.
//
// You can also control the cursor by calling one of:
//  - next()     -- skip next tile (assumed to be the same dimensions as the
//                  previous tile, or the globally-configured width/height
//  - nextRow()  -- skip to the beginning of the next row
// autoAdvance 640, 1024

//
// Define a map-mutator.
//
// If your tileset requires that the map have certain characteristics that it *might*
// not have, you can define one or more map-mutators.
//
// Each map-mutator implicitly receives a map and a current-cell-location. It is free to
// use the provided functions to query the map for that cell or surrounding cells, and
// make any necessary changes to the map (in the form of altitude- or flavor-adjustments).
//
// All mutators are applied in the same order that they're defined here.
//
mutator {
	//
	// Ensure all 0-altitude corners are marked as water
	for(def corner in [TOP, RIGHT, BOTTOM, LEFT])
		if( alt(corner) == 0 )
			setFlavor corner, 'water'
}

mutator {
	//
	// Ensure 0-altitude vertices bordering higher-altitude vertices
	// are marked as grass
	def hasHigherAltitude = { dx,dy ->
		for(def corner in [TOP,RIGHT,BOTTOM,LEFT])
			if(alt(dx,dy,corner) >= 1)
				return true
		false
	}
	
	if(hasHigherAltitude(-1,-1))
		setFlavor LEFT, 'grass'
	if(hasHigherAltitude(-1,0)) {
		setFlavor TOP, 'grass'
		setFlavor LEFT, 'grass'
	}
	if(hasHigherAltitude(-1,+1))
		setFlavor TOP, 'grass'
	if(hasHigherAltitude(0,+1)) {
		setFlavor TOP, 'grass'
		setFlavor RIGHT, 'grass'
	}
	if(hasHigherAltitude(+1,+1))
		setFlavor RIGHT, 'grass'
	if(hasHigherAltitude(+1,0)) {
		setFlavor BOTTOM, 'grass'
		setFlavor RIGHT, 'grass'
	}
	if(hasHigherAltitude(+1,-1))
		setFlavor BOTTOM, 'grass'
	if(hasHigherAltitude(0,-1)) {
		setFlavor BOTTOM, 'grass'
		setFlavor LEFT, 'grass'
	}
}

//
// Define a tile-rule helper
// These become available as additional functions you can call
// directly within tile rules and tile-set mutators.
// Just make sure you define it before you use it.
//
ruleHelper 'countHigherAltitude', { corner ->
	def count = 0
	for(def otherCorner in [TOP, RIGHT, BOTTOM, LEFT])
		if(alt(corner) < alt(otherCorner))
			count++
	count
}

//
// Define a tile
// Inherits fields from its enclosing tile-set:
//  - x/y
//  - width/height
//  - offset
//  - padding
//  - filename
//  - decoration-flag
//  - base
//

tile {
	//
	// Each tile needs an ID. This is a user-readable string you can use
	// to identify this tile later, if need be.
	id = "grass-flat"
	//
	// If you haven't specified [filename] in the surrounding tile-set, you need
	// to define it here
	filename = "landscapeTiles_067.png"
	//
	// Each tile provides one or more "flavors" to each of its corners.
	// Each "provides" definition is additive to those before it.
	// You can specify one or more flavors for all corners simultaneously, like this --
	provides 'grass'
	//
	// -- or you can specify a flavor-list for one or more corners
	provides [TOP, RIGHT, BOTTOM, LEFT],  ['grass']
	//
	// Each tile needs to have at least one rule defined.
	// These rules control where this tile allows itself to be placed on the map.
	// Most commonly, you'd specify this tile's altitude-requirements here, but
	// you can express more complicated conditions, too.
	rule {
		isFlat() || countHigherAltitude(TOP) == 3 || countHigherAltitude(RIGHT) == 3 || countHigherAltitude(BOTTOM) == 3 || countHigherAltitude(LEFT) == 3
	}
}

tile {
	id = 'dirt-flat'
	filename = 'landscapeTiles_083.png'
	provides 'dirt'
	rule { isFlat() }
}

tile {
	id = "sand-flat"
	filename = 'landscapeTiles_059.png'
	provides 'sand'
	rule { isFlat() }
}

tile {
	id = 'open-water'
	filename = 'landscapeTiles_066.png'
	provides 'water'
	rule { isFlat() }
}

tile {
	id = 'grass-slope-north'
	filename = 'landscapeTiles_098.png'
	provides 'grass'
	base = LEFT
	rule {
		alt(LEFT) == alt(BOTTOM) && altDelta(LEFT, [TOP,RIGHT], +1)
	}
}

tile {
	id = 'grass-slope-east'
	filename = 'landscapeTiles_091.png'
	provides 'grass'
	rule {
		alt(LEFT) == alt(TOP) && altDelta(TOP, [BOTTOM,RIGHT], +1)
	}
}

tile {
	id = 'grass-slope-west'
	filename = 'landscapeTiles_106.png'
	provides 'grass'
	base = RIGHT
	rule {
		alt(RIGHT) == alt(BOTTOM) && altDelta(RIGHT, [TOP,LEFT], +1)
	}
}

tile {
	id = 'grass-slope-south'
	filename = 'landscapeTiles_099.png'
	provides 'grass'
	rule {
		alt(RIGHT) == alt(TOP) && altDelta(RIGHT, [BOTTOM,LEFT], +1)
	}
}

tile {
	id = 'grass-slope-top'
	filename = 'landscapeTiles_036.png'
	provides 'grass'
	base = LEFT
	rule {
		altDelta(TOP, [RIGHT,BOTTOM,LEFT], -1)
	}
}

tile {
	id = 'grass-slope-right'
	filename = 'landscapeTiles_028.png'
	provides 'grass'
	rule {
		altDelta(RIGHT, [TOP,BOTTOM,LEFT], -1)
	}
}

tile {
	id = 'grass-slope-bottom'
	filename = 'landscapeTiles_021.png'
	provides 'grass'
	rule {
		altDelta(BOTTOM, [TOP,RIGHT,LEFT], -1)
	}
}

tile {
	id = 'grass-slope-left'
	filename = 'landscapeTiles_029.png'
	provides 'grass'
	rule {
		altDelta(LEFT, [TOP,RIGHT,BOTTOM], -1)
	}
}

tile {
	id = 'water-beach-north'
	filename = 'landscapeTiles_027.png'
	base = LEFT
	provides [LEFT, BOTTOM], ['water']
	provides [TOP, RIGHT], ['sand']
	rule { isFlat() }
}

tile {
	id = 'water-beach-east'
	filename = 'landscapeTiles_035.png'
	provides [LEFT, TOP], ['water']
	provides [BOTTOM, RIGHT], ['sand']
	rule { isFlat() }
}

tile {
	id = 'water-beach-south'
	filename = 'landscapeTiles_042.png'
	provides [TOP, RIGHT], ['water']
	provides [LEFT, BOTTOM], ['sand']
	rule { isFlat() }
}

tile {
	id = 'water-beach-west'
	filename = 'landscapeTiles_034.png'
	base = RIGHT
	provides [BOTTOM, RIGHT], ['water']
	provides [LEFT, TOP], ['sand']
	rule { isFlat() }
}

tile {
	id = 'water-beach-corner-top'
	filename = 'landscapeTiles_068.png'
	base = LEFT
	provides [TOP], ['sand']
	provides [RIGHT, BOTTOM, LEFT], ['water']
	rule { isFlat() }
}

tile {
	id = 'water-beach-corner-right'
	filename = 'landscapeTiles_060.png'
	provides [RIGHT], ['sand']
	provides [TOP, BOTTOM, LEFT], ['water']
	rule { isFlat() }
}

tile {
	id = 'water-beach-corner-bottom'
	filename = 'landscapeTiles_053.png'
	provides [BOTTOM], ['sand']
	provides [TOP, RIGHT, LEFT], ['water']
	rule { isFlat() }
}

tile {
	id = 'water-beach-corner-left'
	filename = 'landscapeTiles_061.png'
	provides [LEFT], ['sand']
	provides [TOP, RIGHT, BOTTOM], ['water']
	rule { isFlat() }
}

tile {
	id = 'water-grass-north'
	filename = 'landscapeTiles_043.png'
	base = LEFT
	provides [LEFT, BOTTOM], ['water']
	provides [TOP, RIGHT], ['grass']
	rule { isFlat() }
}

tile {
	id = 'water-grass-east'
	filename = 'landscapeTiles_051.png'
	provides [LEFT, TOP], ['water']
	provides [BOTTOM, RIGHT], ['grass']
	rule { isFlat() }
}

tile {
	id = 'water-grass-south'
	filename = 'landscapeTiles_058.png'
	provides [TOP, RIGHT], ['water']
	provides [LEFT, BOTTOM], ['grass']
	rule { isFlat() }
}

tile {
	id = 'water-grass-west'
	filename = 'landscapeTiles_050.png'
	base = RIGHT
	provides [BOTTOM, RIGHT], ['water']
	provides [LEFT, TOP], ['grass']
	rule { isFlat() }
}

tile {
	id = 'water-grass-corner-top'
	filename = 'landscapeTiles_084.png'
	base = LEFT
	provides [TOP], ['grass']
	provides [RIGHT, BOTTOM, LEFT], ['water']
	rule { isFlat() }
}

tile {
	id = 'water-grass-corner-right'
	filename = 'landscapeTiles_076.png'
	provides [RIGHT], ['grass']
	provides [TOP, BOTTOM, LEFT], ['water']
	rule { isFlat() }
}

tile {
	id = 'water-grass-corner-bottom'
	filename = 'landscapeTiles_069.png'
	provides [BOTTOM], ['grass']
	provides [TOP, RIGHT, LEFT], ['water']
	rule { isFlat() }
}

tile {
	id = 'water-grass-corner-left'
	filename = 'landscapeTiles_077.png'
	provides [LEFT], ['grass']
	provides [TOP, RIGHT, BOTTOM], ['water']
	rule { isFlat() }
}

tile {
	id = 'grass-beach-water-corner-top'
	filename = 'landscapeTiles_056.png'
	provides [TOP], ['water']
	provides [LEFT,RIGHT], ['sand']
	provides [BOTTOM], ['grass']
	rule { isFlat() }
}

tile {
	id = 'grass-beach-water-corner-right'
	filename = 'landscapeTiles_048.png'
	provides [RIGHT], ['water']
	provides [TOP,BOTTOM], ['sand']
	provides [LEFT], ['grass']
	rule { isFlat() }
}

tile {
	id = 'grass-beach-water-corner-bottom'
	filename = 'landscapeTiles_041.png'
	provides [BOTTOM], ['water']
	provides [LEFT,RIGHT], ['sand']
	provides [TOP], ['grass']
	rule { isFlat() }
}

tile {
	id = 'grass-beach-water-corner-left'
	filename = 'landscapeTiles_049.png'
	provides [LEFT], ['water']
	provides [TOP,BOTTOM], ['sand']
	provides [RIGHT], ['grass']
	rule { isFlat() }
}

tile {
	id = 'grass-water-corner-top'
	filename = 'landscapeTiles_070.png'
	provides [TOP], ['water']
	provides [LEFT,RIGHT,BOTTOM], ['grass']
	rule { isFlat() }
}

tile {
	id = 'grass-water-corner-right'
	filename = 'landscapeTiles_062.png'
	provides [RIGHT], ['water']
	provides [TOP,LEFT,BOTTOM], ['grass']
	rule { isFlat() }
}

tile {
	id = 'grass-water-corner-bottom'
	filename = 'landscapeTiles_055.png'
	provides [BOTTOM], ['water']
	provides [LEFT,RIGHT,TOP], ['grass']
	rule { isFlat() }
}

tile {
	id = 'grass-water-corner-left'
	filename = 'landscapeTiles_063.png'
	provides [LEFT], ['water']
	provides [TOP,RIGHT,BOTTOM], ['grass']
	rule { isFlat() }
}