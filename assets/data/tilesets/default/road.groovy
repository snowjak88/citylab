
decoration = true
base = TOP
surfaceOffset = 0

tile {
	id = 'road-flat-4way'
	filename = 'road-flat-4way.png'
	rule { isFlat() }
	ext.road = [ NORTH, SOUTH, EAST, WEST ]
}

//
//
//

tile {
	id = 'road-flat-curve00'
	filename = 'road-flat-curve00.png'
	rule { isFlat() }
	ext.road = [ NORTH, WEST ]
}

tile {
	id = 'road-flat-curve01'
	filename = 'road-flat-curve01.png'
	rule { isFlat() }
	ext.road = [ SOUTH, WEST ]
}

tile {
	id = 'road-flat-curve02'
	filename = 'road-flat-curve02.png'
	rule { isFlat() }
	ext.road = [ SOUTH, EAST ]
}

tile {
	id = 'road-flat-curve03'
	filename = 'road-flat-curve03.png'
	rule { isFlat() }
	ext.road = [ NORTH, EAST ]
}

//
//
//

tile {
	id = 'road-flat-straight00'
	filename = 'road-flat-straight00.png'
	rule { isFlat() }
	ext.road = [ EAST, WEST ]
}

tile {
	id = 'road-flat-straight01'
	filename = 'road-flat-straight01.png'
	rule { isFlat() }
	ext.road = [ NORTH, SOUTH ]
}

//
//
//

tile {
	id = 'road-flat-t00'
	filename = 'road-flat-t00.png'
	rule { isFlat() }
	ext.road = [ NORTH, EAST, WEST ]
}

tile {
	id = 'road-flat-t01'
	filename = 'road-flat-t01.png'
	rule { isFlat() }
	ext.road = [ NORTH, WEST, SOUTH ]
}

tile {
	id = 'road-flat-t02'
	filename = 'road-flat-t02.png'
	rule { isFlat() }
	ext.road = [ EAST, WEST, SOUTH ]
}

tile {
	id = 'road-flat-t03'
	filename = 'road-flat-t03.png'
	rule { isFlat() }
	ext.road = [ NORTH, SOUTH, EAST ]
}

//
//
//

tile {
	id = 'road-slope-straight00'
	filename = 'road-slope-straight00.png'
	rule {
		altDelta(TOP, [RIGHT], 0) && altDelta(TOP, [LEFT, BOTTOM], +1)
	}
	ext.road = [ SOUTH, NORTH ]
}

tile {
	id = 'road-slope-straight01'
	filename = 'road-slope-straight01.png'
	rule {
		altDelta(TOP, [LEFT], 0) && altDelta(TOP, [RIGHT, BOTTOM], +1)
	}
	ext.road = [ WEST, EAST ]
}

tile {
	id = 'road-slope-straight02'
	filename = 'road-slope-straight02.png'
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [LEFT], 0) && altDelta(BOTTOM, [RIGHT, TOP], +1)
	}
	ext.road = [ SOUTH, NORTH ]
}

tile {
	id = 'road-slope-straight03'
	filename = 'road-slope-straight03.png'
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [RIGHT], 0) && altDelta(BOTTOM, [LEFT, TOP], +1)
	}
	ext.road = [ WEST, EAST ]
}