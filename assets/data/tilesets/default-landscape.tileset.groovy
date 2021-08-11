title = "Default landscape tileset"
description = "Default landscape tileset, being the developer's first Blender project."

//
// The grid-dimensions for which this tileset was designed
//
// (Default: 32 / 16)
gridWidth = 196
gridHeight = 97

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
surfaceOffset = 37

//
// Every unit of altitude should add/subtract these many pixels from this tile's Y-position.
altitudeOffset = 31

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
// Default file-name for all tiles
//
// (Default: no default)
// filename = "default.png"

//
// All image-references will be made relative to the given sub-directory.
// This sub-directory must be in the same directory as this descriptor.
//
// (Default: no default)
folder = "default/landscape"

tile {
	id = 'grass-flat'
	filename = 'grass-flat.png'
	provides 'grass'
	rule {
		isFlat()
	}
}

tile {
	id = 'grass-slope-south'
	filename = 'grass-slope00.png'
	provides 'grass'
	rule {
		alt(TOP) == alt(RIGHT) && altDelta(TOP, [BOTTOM,LEFT], +1)
	}
}

tile {
	id = 'grass-slope-east'
	filename = 'grass-slope01.png'
	provides 'grass'
	rule {
		alt(TOP) == alt(LEFT) && altDelta(TOP, [BOTTOM,RIGHT], +1)
	}
}

tile {
	id = 'grass-slope-north'
	filename = 'grass-slope02.png'
	provides 'grass'
	base = LEFT
	rule {
		alt(LEFT) == alt(BOTTOM) && altDelta(LEFT, [TOP,RIGHT], +1)
	}
}

tile {
	id = 'grass-slope-west'
	filename = 'grass-slope03.png'
	provides 'grass'
	base = RIGHT
	rule {
		alt(RIGHT) == alt(BOTTOM) && altDelta(RIGHT, [TOP,LEFT], +1)
	}
}

tile {
	id = 'grass-slope-concave-south'
	filename = 'grass-slope-concave00.png'
	provides 'grass'
	rule {
		altDelta(TOP, [RIGHT,BOTTOM,LEFT], +1)
	}
}

tile {
	id = 'grass-slope-concave-east'
	filename = 'grass-slope-concave01.png'
	provides 'grass'
	base = LEFT
	rule {
		altDelta(LEFT, [RIGHT,BOTTOM,TOP], +1)
	}
}

tile {
	id = 'grass-slope-concave-north'
	filename = 'grass-slope-concave02.png'
	provides 'grass'
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [RIGHT,TOP,LEFT], +1)
	}
}

tile {
	id = 'grass-slope-concave-west'
	filename = 'grass-slope-concave03.png'
	provides 'grass'
	base = RIGHT
	rule {
		altDelta(RIGHT, [TOP,BOTTOM,LEFT], +1)
	}
}

tile {
	id = 'grass-slope-convex-west'
	filename = 'grass-slope-convex00.png'
	provides 'grass'
	rule {
		altDelta(LEFT, [TOP,BOTTOM,RIGHT], -1)
	}
}

tile {
	id = 'grass-slope-convex-south'
	filename = 'grass-slope-convex01.png'
	provides 'grass'
	rule {
		altDelta(BOTTOM, [TOP,LEFT,RIGHT], -1)
	}
}

tile {
	id = 'grass-slope-convex-east'
	filename = 'grass-slope-convex02.png'
	provides 'grass'
	rule {
		altDelta(RIGHT, [TOP,BOTTOM,LEFT], -1)
	}
}

tile {
	id = 'grass-slope-convex-north'
	filename = 'grass-slope-convex03.png'
	provides 'grass'
	base = RIGHT
	rule {
		altDelta(TOP, [RIGHT,BOTTOM,LEFT], -1)
	}
}