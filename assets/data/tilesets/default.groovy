
id = "default"
title = "Default tileset"
description = "Default world tileset. Created by darkrose (https://opengameart.org/users/darkrose)."

//
// The grid-dimensions for which this tileset can be made to work
//
// (Default: 32 / 16)
gridWidth = 64
gridHeight = 32

//
// Default width/height for all tiles. (Visible width/height, as distinct from grid width/height.)
//
// (Default: 32 / 32)
width = 64
height = 64

//
// Default file-name for all tiles
// Must be in the same directory as this descriptor
filename = "default.png"

//
// Starting x/y
//
// (Default: 0 / 0)
x = 0
y = 0

//
// Drawing offset (along Y axis, from the tile's bottom edge)
// i.e., how far should the tile be offset so its visual "bottom" corner
// is aligned with the grid-cell's "bottom" corner
//
// Useful if your tiles contain some "subsurface" detail that you want
// to be positioned "below" the grid.
//
// (Default: 0)
offset = 32

//
// Each tile needs to be drawn with a vertical offset depending on its altitude.
// The tile-renderer should consult this corner of each tile to determine which
// of that tile's vertices should contribute its altitude to that vertical offset.
base = TOP

//
// Each tile's image has so much padding (empty space surrounding
// the "meat" of the tile, and which should be ignored)
//
// (Default: 0)
padding = 0

//
// "Auto-advance" x/y for every subsequent tile
// Because [filename] is not loaded right when the script is executed,
// you need to supply the image dimensions here
//
// Once set up, the "cursor" will iterate to the next tile-slot with every
// tile definition
//
// You can also control the cursor by calling one of:
//  - next()     -- skip next tile (assumed to be the same dimensions as the
//                  previous tile, or the globally-configured width/height
//  - nextRow()  -- skip to the beginning of the next row
autoAdvance 640, 1024

//
// As it happens, we need to skip the very first tile-cell
next()

//
// Define a tile
// Inherits fields from its enclosing tile-set:
//  - x/y
//  - width/height
//  - offset
//  - padding
//  - filename
//
// You may certainly override them here, if you need them to be specific
tile {
	id = "A"
	provides 'grass', 'water'
	rule { isFlat() }
}

tile {
	id = "B"
	provides 'grass', 'water'
	rule { isFlat() }
}

tile {
	id = "C"
	provides 'grass'
	rule { isFlat() }
}

tile {
	id = "D"
	provides 'grass'
	rule { isFlat() }
}

tile {
	id = "E"
	provides [RIGHT], ['grass', 'water']
	provides [TOP, BOTTOM, LEFT], ['grass']
	rule { isFlat() }
}

tile {
	id = "F"
	provides 'grass'
	rule { isFlat() }
}

tile {
	id = "G"
	provides [TOP, LEFT], ['grass', 'water']
	provides [RIGHT, BOTTOM], ['grass']
	rule { isFlat() }
}

tile {
	id = "H"
	provides [BOTTOM, LEFT], ['grass', 'water']
	provides [TOP, RIGHT], ['grass']
	rule { isFlat() }
}

tile {
	id = "I"
	provides [BOTTOM, RIGHT], ['grass', 'water']
	provides [TOP, LEFT], ['grass']
	rule { isFlat() }
}

tile {
	id = "J"
	provides [TOP], ['grass', 'water']
	provides [RIGHT, BOTTOM, LEFT], ['grass']
	rule { isFlat() }
}

tile {
	id = "K"
	provides [RIGHT], ['grass', 'water']
	provides [TOP, BOTTOM, LEFT], ['grass']
	rule { isFlat() }
}

tile {
	id = "L"
	provides [BOTTOM], ['grass', 'water']
	provides [TOP, RIGHT, LEFT], ['grass']
	rule { isFlat() }
}

tile {
	id = "M"
	provides [LEFT], ['grass', 'water']
	provides [TOP, BOTTOM, RIGHT], ['grass']
	rule { isFlat() }
}

tile {
	id = "N"
	provides [RIGHT, BOTTOM, LEFT], ['grass', 'water']
	provides [TOP], ['grass']
	rule { isFlat() }
}

tile {
	id = "O"
	provides [TOP, BOTTOM, LEFT], ['grass', 'water']
	provides [RIGHT], ['grass']
	rule { isFlat() }
}

tile {
	id = "P"
	provides [TOP, RIGHT, LEFT], ['grass', 'water']
	provides [BOTTOM], ['grass']
	rule { isFlat() }
}

tile {
	id = "Q"
	provides [TOP, RIGHT, BOTTOM], ['grass', 'water']
	provides [LEFT], ['grass']
	rule { isFlat() }
}

tile {
	id = "R"
	provides [TOP, RIGHT], ['grass', 'water']
	provides [BOTTOM, LEFT], ['grass']
	rule { isFlat() }
}

tile {
	id = "S"
	provides [TOP, LEFT], ['grass', 'water']
	provides [RIGHT, BOTTOM], ['grass']
	rule { isFlat() }
}

nextRow()
next()
next()
next()
next()

tile {
	id = "AG"
	provides 'grass'
	rule { isFlat() }
}

offset = 0

tile {
	id = "AH"
	provides 'grass'
	rule { isFlat([TOP,RIGHT,BOTTOM]) && altDelta(TOP, [LEFT], +1) }
}

tile {
	id = "AI"
	provides 'grass'
	rule { isFlat([TOP,LEFT]) && altDelta(TOP, [RIGHT, BOTTOM], +1) }
}

tile {
	id = "AJ"
	provides 'grass'
	rule { isFlat([TOP,RIGHT]) && altDelta(TOP, [LEFT, BOTTOM], +1) }
}

tile {
	id = "AK"
	provides 'grass'
	rule { isFlat([TOP,RIGHT, LEFT]) && altDelta(TOP, [BOTTOM], +1) }
}

nextRow()

tile {
	id = "AL"
	provides 'grass'
	offset = 32
	base = RIGHT
	rule { isFlat([RIGHT, LEFT, BOTTOM]) && altDelta(RIGHT, [TOP], -1) }
}

tile {
	id = "AM"
	provides 'grass'
	offset = 32
	rule { isFlat([TOP, LEFT, BOTTOM]) && altDelta(TOP, [RIGHT], -1) }
}

tile {
	id = "AN"
	provides 'grass'
	offset = 32
	rule { isFlat([TOP, RIGHT, LEFT]) && altDelta(TOP, [BOTTOM], -1) }
}

tile {
	id = "AO"
	provides 'grass'
	offset = 32
	rule { isFlat([TOP, RIGHT, BOTTOM]) && altDelta(TOP, [LEFT], -1) }
}

tile {
	id = "AP"
	provides 'grass'
	offset = 32
	rule { altDelta(TOP, [RIGHT, LEFT, BOTTOM], -1) }
}

tile {
	id = "AQ"
	provides 'grass'
	offset = 32
	rule { isFlat([TOP, RIGHT]) && altDelta(TOP, [LEFT, BOTTOM], -1) }
}

tile {
	id = "AR"
	provides 'grass'
	offset = 32
	rule { isFlat([TOP, LEFT]) && altDelta(TOP, [RIGHT, BOTTOM], -1) }
}

tile {
	id = "AS"
	provides 'grass'
	rule { altDelta(RIGHT, [TOP, LEFT, BOTTOM], -1) }
}

nextRow()
