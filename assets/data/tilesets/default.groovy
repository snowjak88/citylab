
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
// As it happens, we need to skip the very first tile on the spritesheet (as it's blank)
next()

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
//
//tile {
//	id = "A"
//	provides 'grass', 'water'
//	rule { isFlat() }
//}
next()

//tile {
//	id = "B"
//	provides 'grass', 'water'
//	rule { isFlat() }
//}
next()

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

//tile {
//	id = "E"
//	provides [RIGHT], ['grass', 'water']
//	provides [TOP, BOTTOM, LEFT], ['grass']
//	rule { isFlat() }
//}
next()

tile {
	id = "F"
	provides 'grass'
	rule { isFlat() }
}

//tile {
//	id = "G"
//	provides [TOP, LEFT], ['grass', 'water']
//	provides [RIGHT, BOTTOM], ['grass']
//	rule { isFlat() }
//}
next()
//
//tile {
//	id = "H"
//	provides [BOTTOM, LEFT], ['grass', 'water']
//	provides [TOP, RIGHT], ['grass']
//	rule { isFlat() }
//}
next()

//tile {
//	id = "I"
//	provides [BOTTOM, RIGHT], ['grass', 'water']
//	provides [TOP, LEFT], ['grass']
//	rule { isFlat() }
//}
next()

//tile {
//	id = "J"
//	provides [TOP], ['grass', 'water']
//	provides [RIGHT, BOTTOM, LEFT], ['grass']
//	rule { isFlat() }
//}
next()

//tile {
//	id = "K"
//	provides [RIGHT], ['grass', 'water']
//	provides [TOP, BOTTOM, LEFT], ['grass']
//	rule { isFlat() }
//}
next()

//tile {
//	id = "L"
//	provides [BOTTOM], ['grass', 'water']
//	provides [TOP, RIGHT, LEFT], ['grass']
//	rule { isFlat() }
//}
next()

//tile {
//	id = "M"
//	provides [LEFT], ['grass', 'water']
//	provides [TOP, BOTTOM, RIGHT], ['grass']
//	rule { isFlat() }
//}
next()

//tile {
//	id = "N"
//	provides [RIGHT, BOTTOM, LEFT], ['grass', 'water']
//	provides [TOP], ['grass']
//	rule { isFlat() }
//}
next()

//tile {
//	id = "O"
//	provides [TOP, BOTTOM, LEFT], ['grass', 'water']
//	provides [RIGHT], ['grass']
//	rule { isFlat() }
//}
next()

//tile {
//	id = "P"
//	provides [TOP, RIGHT, LEFT], ['grass', 'water']
//	provides [BOTTOM], ['grass']
//	rule { isFlat() }
//}
next()

//tile {
//	id = "Q"
//	provides [TOP, RIGHT, BOTTOM], ['grass', 'water']
//	provides [LEFT], ['grass']
//	rule { isFlat() }
//}
next()

//tile {
//	id = "R"
//	provides [TOP, RIGHT], ['grass', 'water']
//	provides [BOTTOM, LEFT], ['grass']
//	rule { isFlat() }
//}
next()

//tile {
//	id = "S"
//	provides [TOP, LEFT], ['grass', 'water']
//	provides [RIGHT, BOTTOM], ['grass']
//	rule { isFlat() }
//}
next()

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
	rule { isFlat([TOP, RIGHT, BOTTOM]) && altDelta(TOP, [LEFT], +1) }
}

tile {
	id = "AI"
	provides 'grass'
	rule { isFlat([TOP, LEFT]) && altDelta(TOP, [RIGHT, BOTTOM], +1) }
}

tile {
	id = "AJ"
	provides 'grass'
	rule { isFlat([TOP, RIGHT]) && altDelta(TOP, [LEFT, BOTTOM], +1) }
}

tile {
	id = "AK"
	provides 'grass'
	rule { isFlat([TOP, RIGHT, LEFT]) && altDelta(TOP, [BOTTOM], +1) }
}

nextRow()
offset = 32
tile {
	id = "AL"
	provides 'grass'
	base = RIGHT
	rule { isFlat([RIGHT, LEFT, BOTTOM]) && altDelta(RIGHT, [TOP], -1) }
}

tile {
	id = "AM"
	provides 'grass'
	rule { isFlat([TOP, LEFT, BOTTOM]) && altDelta(TOP, [RIGHT], -1) }
}

tile {
	id = "AN"
	provides 'grass'
	rule { isFlat([TOP, RIGHT, LEFT]) && altDelta(TOP, [BOTTOM], -1) }
}

tile {
	id = "AO"
	provides 'grass'
	rule { isFlat([TOP, RIGHT, BOTTOM]) && altDelta(TOP, [LEFT], -1) }
}

tile {
	id = "AP"
	provides 'grass'
	rule { altDelta(TOP, [RIGHT, LEFT, BOTTOM], -1) }
}

tile {
	id = "AQ"
	provides 'grass'
	rule { isFlat([TOP, RIGHT]) && altDelta(TOP, [LEFT, BOTTOM], -1) }
}

tile {
	id = "AR"
	provides 'grass'
	rule { isFlat([TOP, LEFT]) && altDelta(TOP, [RIGHT, BOTTOM], -1) }
}

tile {
	id = "AS"
	offset = 0
	provides 'grass'
	rule { altDelta(RIGHT, [TOP, LEFT, BOTTOM], -1) }
}

nextRow()
nextRow()
nextRow()
nextRow()

//
// Define a rule helper -- a special function that tile-rules can
// use to simplify their syntax.
//
// Once defined, this can be used like any other function within
// a rule-body.
//
// As a reminder of how this coordinate-system is laid out:
//
// (x,y) have their origin at the left corner of the map.
// x increases down-right
// y increases up-left
//
//
ruleHelpers['isCellAboveWater'] = { dx,dy ->
	alt(dx,dy,TOP) > 0 || alt(dx,dy,RIGHT) > 0 || alt(dx,dy,BOTTOM) > 0 || alt(dx,dy,LEFT) > 0
}

//
// This helper lets you check surrounding cells for
// having any of their corners above water.
// flags[][] may contain:
//   0 = must be below water
//   1 = must be above water
//  (anything else) = don't care
ruleHelpers['surroundingAboveWater'] = { flags ->
	for(int dx in -1..+1)
		for(int dy in -1..+1) {
			
			def flag = flags[-dy + 1][dx+1]
			if(flag != 0 && flag != 1)
				continue
			
			def isAbove = isCellAboveWater(dx,dy)
			if(isAbove == (flag == 0) )
				return false
		}
	true
}

decoration = true
tile {
	id = "CE"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[ 0,  0, -1],
			[ 0,  0,  1],
			[-1,  1,  1]	]
		)
	}
}

tile {
	id = "CF"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[ 0,  0,  0],
			[ 0, -1,  0],
			[-1,  1, -1]	]
		)
	}
}

tile {
	id = "CG"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[ 0,  0, -1],
			[ 0, -1,  1],
			[ 0,  0, -1]	]
		)
	}
}

tile {
	id = "CH"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[-1,  0,  0],
			[ 1, -1,  0],
			[ 1,  1, -1]	]
		)
	}
}

tile {
	id = "CI"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[ 0,  0,  0],
			[ 0, -1,  0],
			[ 0,  0,  0]	]
		)
	}
}

tile {
	id = "CJ"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[ 0,  0,  0],
			[ 0, -1,  0],
			[ 0,  0,  0]	]
		)
	}
}

tile {
	id = "CK"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[-1,  1,  1],
			[ 0, -1,  1],
			[ 0,  0, -1]	]
		)
	}
}

tile {
	id = "CL"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[-1,  0,  0],
			[ 1, -1,  0],
			[-1,  0,  0]	]
		)
	}
}

tile {
	id = "CM"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[-1,  1, -1],
			[ 0, -1,  0],
			[ 0,  0,  0]	]
		)
	}
}

tile {
	id = "CN"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[ 1,  1, -1],
			[ 1, -1,  0],
			[-1,  0,  0]	]
		)
	}
}

tile {
	id = "CO"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[ 1,  0, -1],
			[ 0,  0,  1],
			[-1,  1,  1]	]
		)
	}
}

tile {
	id = "CP"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[-1,  1, -1],
			[ 0,  0,  0],
			[-1,  1, -1]	]
		)
	}
}

tile {
	id = "CQ"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[-1,  0, -1],
			[ 1,  0,  1],
			[-1,  0, -1]	]
		)
	}
}

tile {
	id = "CR"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[-1,  0,  1],
			[ 1,  0,  0],
			[ 1,  1, -1]	]
		)
	}
}

tile {
	id = "CS"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[-1,  1,  1],
			[ 0,  0,  1],
			[ 1,  0, -1]	]
		)
	}
}

tile {
	id = "CT"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[ 1,  1, -1],
			[ 1,  0,  0],
			[-1,  0,  1]	]
		)
	}
}

tile {
	id = "CU"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[ 1,  0,  0],
			[ 0,  0,  0],
			[ 0,  0,  0]	]
		)
	}
}

tile {
	id = "CV"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[ 0,  0,  1],
			[ 0,  0,  0],
			[ 0,  0,  0]	]
		)
	}
}

tile {
	id = "CW"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[ 0,  0,  0],
			[ 0,  0,  0],
			[ 1,  0,  0]	]
		)
	}
}

tile {
	id = "CX"
	provides 'water'
	rule {
		isFlat() && alt(TOP) == 0
		&& surroundingAboveWater(
		[	[ 0,  0,  0],
			[ 0,  0,  0],
			[ 0,  0,  1]	]
		)
	}
}