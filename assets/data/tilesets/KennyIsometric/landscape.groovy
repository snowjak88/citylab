//
// Define some landscape/terrain tiles for the including tile-set.
//

transparent = false

tile {
	id = 'grass-flat'
	filename = 'landscapeTiles_067.png'
	rule { isFlat() }
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
}

//
//
//

tile {
	id = 'grass-slope-south'
	filename = 'landscapeTiles_099.png'
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
	rule { alt(TOP) == alt(RIGHT) && altDelta(TOP, [BOTTOM, LEFT], +1) }
}

tile {
	id = 'grass-slope-east'
	filename = 'landscapeTiles_091.png'
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
	rule { alt(TOP) == alt(LEFT) && altDelta(TOP, [BOTTOM, RIGHT], +1) }
}

tile {
	id = 'grass-slope-north'
	filename = 'landscapeTiles_098.png'
	base = LEFT
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
	rule { alt(LEFT) == alt(BOTTOM) && altDelta(LEFT, [TOP, RIGHT], +1) }
}

tile {
	id = 'grass-slope-west'
	filename = 'landscapeTiles_106.png'
	base = RIGHT
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
	rule { alt(RIGHT) == alt(BOTTOM) && altDelta(RIGHT, [TOP, LEFT], +1) }
}

//
//
//

tile {
	id = 'grass-slope-concave-south'
	filename = 'landscapeTiles_067.png'
	base = RIGHT
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
	rule { altDelta(TOP, [RIGHT, BOTTOM, LEFT], +1) }
}

tile {
	id = 'grass-slope-concave-east'
	filename = 'landscapeTiles_067.png'
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
	rule { altDelta(LEFT, [RIGHT, BOTTOM, TOP], +1) }
}

tile {
	id = 'grass-slope-concave-north'
	filename = 'landscapeTiles_067.png'
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
	rule { altDelta(BOTTOM, [RIGHT, TOP, LEFT], +1) }
}

tile {
	id = 'grass-slope-concave-west'
	filename = 'landscapeTiles_067.png'
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
	rule { altDelta(RIGHT, [TOP, BOTTOM, LEFT], +1) }
}

//
//
//

tile {
	id = 'grass-slope-convex-west'
	filename = 'landscapeTiles_029.png'
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
	rule { altDelta(LEFT, [TOP, BOTTOM, RIGHT], -1) }
}

tile {
	id = 'grass-slope-convex-south'
	filename = 'landscapeTiles_021.png'
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
	rule { altDelta(BOTTOM, [TOP, LEFT, RIGHT], -1) }
}

tile {
	id = 'grass-slope-convex-east'
	filename = 'landscapeTiles_028.png'
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
	rule { altDelta(RIGHT, [TOP, BOTTOM, LEFT], -1) }
}

tile {
	id = 'grass-slope-convex-north'
	filename = 'landscapeTiles_036.png'
	base = RIGHT
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
	rule { altDelta(TOP, [RIGHT, BOTTOM, LEFT], -1) }
}

//
//
//

tile {
	id = 'grass-slope-saddle-ns'
	filename = 'landscapeTiles_067.png'
	base = RIGHT
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
	rule { altDelta(TOP, [BOTTOM], 0) && altDelta(TOP, [RIGHT, LEFT], +1) }
}

tile {
	id = 'grass-slope-saddle-ew'
	filename = 'landscapeTiles_067.png'
	base = TOP
	rule { !ext.road }
	rule { !ext.water }
	rule { ext.terrain == 'grass' }
	rule { altDelta(LEFT, [RIGHT], 0) && altDelta(LEFT, [TOP, BOTTOM], +1) }
}