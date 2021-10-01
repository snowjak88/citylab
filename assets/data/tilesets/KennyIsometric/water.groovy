
transparent = false

tile {
	id = 'water-flat'
	filename = 'landscapeTiles_066.png'
	rule { !ext.road }
	rule { isFlat() }
	rule { listsMatch( ext.water, [LEFT, RIGHT, TOP, BOTTOM] ) }
}

//
//
//

tile {
	id = 'water-shore-north'
	filename = 'landscapeTiles_043.png'
	base = LEFT
	rule { !ext.road }
	rule { ext.terrain == 'grass' }
	rule { isFlat() }
	rule { listsMatch( ext.water, [LEFT, BOTTOM] ) }
}

tile {
	id = 'water-shore-east'
	filename = 'landscapeTiles_051.png'
	base = LEFT
	rule { !ext.road }
	rule { ext.terrain == 'grass' }
	rule { isFlat() }
	rule { listsMatch( ext.water, [LEFT, TOP] ) }
}

tile {
	id = 'water-shore-south'
	filename = 'landscapeTiles_058.png'
	rule { !ext.road }
	rule { ext.terrain == 'grass' }
	rule { isFlat() }
	rule { listsMatch( ext.water, [RIGHT, TOP] ) }
}

tile {
	id = 'water-shore-west'
	filename = 'landscapeTiles_050.png'
	base = RIGHT
	rule { !ext.road }
	rule { ext.terrain == 'grass' }
	rule { isFlat() }
	rule { listsMatch( ext.water, [RIGHT, BOTTOM] ) }
}

//
//
//

tile {
	id = 'water-shore-concave-top'
	filename = 'landscapeTiles_070.png'
	rule { !ext.road }
	rule { ext.terrain == 'grass' }
	rule { isFlat() }
	rule { listsMatch( ext.water, [TOP] ) }
}

tile {
	id = 'water-shore-concave-right'
	filename = 'landscapeTiles_062.png'
	base = RIGHT
	rule { !ext.road }
	rule { ext.terrain == 'grass' }
	rule { isFlat() }
	rule { listsMatch( ext.water, [RIGHT] ) }
}

tile {
	id = 'water-shore-concave-bottom'
	filename = 'landscapeTiles_055.png'
	base = BOTTOM
	rule { !ext.road }
	rule { ext.terrain == 'grass' }
	rule { isFlat() }
	rule { listsMatch( ext.water, [BOTTOM] ) }
}

tile {
	id = 'water-shore-concave-left'
	filename = 'landscapeTiles_063.png'
	base = LEFT
	rule { !ext.road }
	rule { ext.terrain == 'grass' }
	rule { isFlat() }
	rule { listsMatch( ext.water, [LEFT] ) }
}

//
//
//

tile {
	id = 'water-shore-corner-top'
	filename = 'landscapeTiles_084.png'
	base = LEFT
	rule { !ext.road }
	rule { ext.terrain == 'grass' }
	rule { isFlat() }
	rule { listsMatch( ext.water, [LEFT, RIGHT, BOTTOM] ) }
}

tile {
	id = 'water-shore-corner-right'
	filename = 'landscapeTiles_076.png'
	rule { !ext.road }
	rule { ext.terrain == 'grass' }
	rule { isFlat() }
	rule { listsMatch( ext.water, [LEFT, TOP, BOTTOM] ) }
}

tile {
	id = 'water-shore-corner-bottom'
	filename = 'landscapeTiles_069.png'
	rule { !ext.road }
	rule { ext.terrain == 'grass' }
	rule { isFlat() }
//	rule { altDelta(BOTTOM, [LEFT, RIGHT, TOP], -1) }
	rule { listsMatch( ext.water, [LEFT, RIGHT, TOP] ) }
}

tile {
	id = 'water-shore-corner-left'
	filename = 'landscapeTiles_077.png'
	rule { !ext.road }
	rule { ext.terrain == 'grass' }
	rule { isFlat() }
	rule { listsMatch( ext.water, [RIGHT, TOP, BOTTOM] ) }
}