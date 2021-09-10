id = "default"

title = "Default tileset"
description = "Default tileset, being the developer's first Blender project."

//
// The grid-dimensions for which this tileset was designed
//
//
// (Default: 32 / 16)
gridWidth = 128
gridHeight = 64

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
// Texture-atlas to use
//
// If you supply an atlas here, then all filenames are assumed to refer to regions within
// that atlas.
// If atlas is <null>, then "filename" is assumed to be a discrete image-file.
//
atlas = 'default/default.atlas'

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
// folder = "default/landscape"
// folder = file("default/landscape")

//
// Include all tile-definitions contained in the given script.
// Script file-name is relative to the script's directory, *not* [folder]
//
// The included definition inherits *most* attributes from this definition:
//   * width, height
//   * padding
//   * gridWidth, gridHeight
//   * surfaceOffset, altitudeOffset
//   * base
//   * decoration
//   * rule-helpers
//
// In fact: the set of rule-helpers is shared both ways. You can
// define rule-helpers in an included script and use them in the includer.
//
include 'default/landscape/landscape.groovy'

include 'default/water/water.groovy'