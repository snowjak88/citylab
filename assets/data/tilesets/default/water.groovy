
decoration = true
surfaceOffset = 0

tile {
	id = 'water-flat-west'
	filename = 'water-flat-00.png'
	base = LEFT
	rule {
		altDelta(LEFT, [TOP, BOTTOM], 0) && alt(LEFT) <= alt(RIGHT)
	}
	ext.water = [ LEFT ]
}
tile {
	id = 'water-flat-south'
	filename = 'water-flat-01.png'
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [RIGHT, LEFT], 0) && alt(BOTTOM) <= alt(TOP)
	}
	ext.water = [ BOTTOM ]
}
tile {
	id = 'water-flat-east'
	filename = 'water-flat-02.png'
	base = RIGHT
	rule {
		altDelta(RIGHT, [TOP, BOTTOM], 0) && alt(RIGHT) <= alt(LEFT)
	}
	ext.water = [ RIGHT ]
}
tile {
	id = 'water-flat-north'
	filename = 'water-flat-03.png'
	base = TOP
	rule {
		altDelta(TOP, [RIGHT, LEFT], 0) && alt(TOP) <= alt(BOTTOM)
	}
	ext.water = [ TOP ]
}
tile {
	id = 'water-flat-all'
	filename = 'water-flat-04.png'
	base = TOP
	rule {
		isFlat()
	}
	ext.water = [ TOP, RIGHT, LEFT, BOTTOM ]
}

//
//
//

tile {
	id = 'water-saddle-flat-south'
	filename = 'water-saddle-flat-00.png'
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [TOP], 0) && altDelta(BOTTOM, [RIGHT,LEFT], +1)
	}
	ext.water = [ BOTTOM ]
}
tile {
	id = 'water-saddle-flat-east'
	filename = 'water-saddle-flat-01.png'
	base = RIGHT
	rule {
		altDelta(RIGHT, [LEFT], 0) && altDelta(RIGHT, [TOP,BOTTOM], +1)
	}
	ext.water = [ RIGHT ]
}
tile {
	id = 'water-saddle-flat-north'
	filename = 'water-saddle-flat-02.png'
	base = TOP
	rule {
		altDelta(TOP, [BOTTOM], 0) && altDelta(TOP, [RIGHT,LEFT], +1)
	}
	ext.water = [ TOP ]
}
tile {
	id = 'water-saddle-flat-west'
	filename = 'water-saddle-flat-03.png'
	base = LEFT
	rule {
		altDelta(LEFT, [RIGHT], 0) && altDelta(LEFT, [TOP,BOTTOM], +1)
	}
	ext.water = [ LEFT ]
}

tile {
	id = 'water-saddle-flat-north-south'
	filename = 'water-saddle-flat-04.png'
	base = TOP
	rule {
		altDelta(TOP, [BOTTOM], 0) && altDelta(TOP, [RIGHT,LEFT], +1)
	}
	ext.water = [ TOP, BOTTOM ]
}
tile {
	id = 'water-saddle-flat-east-west'
	filename = 'water-saddle-flat-05.png'
	base = LEFT
	rule {
		altDelta(LEFT, [RIGHT], 0) && altDelta(LEFT, [TOP,BOTTOM], +1)
	}
	ext.water = [ LEFT, RIGHT ]
}

//
//
//

tile {
	id = 'water-slope-concave-flat-north'
	filename = 'water-slope-concave-flat-00.png'
	base = TOP
	rule {
		altDelta(TOP, [RIGHT, BOTTOM, LEFT], +1)
	}
	ext.water = [ TOP ]
}
tile {
	id = 'water-slope-concave-flat-west'
	filename = 'water-slope-concave-flat-01.png'
	base = LEFT
	rule {
		altDelta(LEFT, [RIGHT, BOTTOM, TOP], +1)
	}
	ext.water = [ LEFT ]
}
tile {
	id = 'water-slope-concave-flat-south'
	filename = 'water-slope-concave-flat-02.png'
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [RIGHT, TOP, LEFT], +1)
	}
	ext.water = [ TOP ]
}
tile {
	id = 'water-slope-concave-flat-east'
	filename = 'water-slope-concave-flat-03.png'
	base = RIGHT
	rule {
		altDelta(RIGHT, [LEFT, BOTTOM, TOP], +1)
	}
	ext.water = [ RIGHT ]
}

//
//
//

tile {
	id = 'water-slope-convex-flat-east'
	filename = 'water-slope-convex-flat-00.png'
	base = RIGHT
	rule {
		altDelta(BOTTOM, [RIGHT, TOP], 0) && altDelta(BOTTOM, [LEFT], +1)
	}
	ext.water = [ TOP, RIGHT, BOTTOM ]
}
tile {
	id = 'water-slope-convex-flat-north'
	filename = 'water-slope-convex-flat-01.png'
	base = TOP
	rule {
		altDelta(TOP, [RIGHT, LEFT], 0) && altDelta(TOP, [BOTTOM], +1)
	}
	ext.water = [ TOP, RIGHT, LEFT ]
}
tile {
	id = 'water-slope-convex-flat-west'
	filename = 'water-slope-convex-flat-02.png'
	base = LEFT
	rule {
		altDelta(LEFT, [TOP, BOTTOM], 0) && altDelta(LEFT, [RIGHT], +1)
	}
	ext.water = [ TOP, LEFT, BOTTOM ]
}
tile {
	id = 'water-slope-convex-flat-south'
	filename = 'water-slope-convex-flat-03.png'
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [RIGHT, LEFT], 0) && altDelta(BOTTOM, [TOP], +1)
	}
	ext.water = [ RIGHT, LEFT, BOTTOM ]
}

tile {
	id = 'water-slope-convex-flat-east-left'
	filename = 'water-slope-convex-flat-left-00.png'
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [RIGHT, TOP], 0) && altDelta(BOTTOM, [LEFT], +1)
	}
	ext.water = [ BOTTOM ]
}
tile {
	id = 'water-slope-convex-flat-north-left'
	filename = 'water-slope-convex-flat-left-01.png'
	base = RIGHT
	rule {
		altDelta(RIGHT, [TOP, LEFT], 0) && altDelta(RIGHT, [BOTTOM], +1)
	}
	ext.water = [ RIGHT ]
}
tile {
	id = 'water-slope-convex-flat-west-left'
	filename = 'water-slope-convex-flat-left-02.png'
	base = TOP
	rule {
		altDelta(TOP, [LEFT, BOTTOM], 0) && altDelta(TOP, [RIGHT], +1)
	}
	ext.water = [ TOP ]
}
tile {
	id = 'water-slope-convex-flat-south-left'
	filename = 'water-slope-convex-flat-left-03.png'
	base = LEFT
	rule {
		altDelta(LEFT, [BOTTOM, RIGHT], 0) && altDelta(LEFT, [TOP], +1)
	}
	ext.water = [ LEFT ]
}

tile {
	id = 'water-slope-convex-flat-east-right'
	filename = 'water-slope-convex-flat-right-00.png'
	base = TOP
	rule {
		altDelta(TOP, [RIGHT, BOTTOM], 0) && altDelta(TOP, [LEFT], +1)
	}
	ext.water = [ TOP ]
}
tile {
	id = 'water-slope-convex-flat-north-right'
	filename = 'water-slope-convex-flat-right-01.png'
	base = LEFT
	rule {
		altDelta(LEFT, [TOP, RIGHT], 0) && altDelta(LEFT, [BOTTOM], +1)
	}
	ext.water = [ LEFT ]
}
tile {
	id = 'water-slope-convex-flat-west-right'
	filename = 'water-slope-convex-flat-right-02.png'
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [LEFT, TOP], 0) && altDelta(BOTTOM, [RIGHT], +1)
	}
	ext.water = [ BOTTOM ]
}
tile {
	id = 'water-slope-convex-flat-south-right'
	filename = 'water-slope-convex-flat-right-03.png'
	base = RIGHT
	rule {
		altDelta(RIGHT, [BOTTOM, LEFT], 0) && altDelta(RIGHT, [TOP], +1)
	}
	ext.water = [ RIGHT ]
}

//
//
//

tile {
	id = 'water-slope-flat-northeast'
	filename = 'water-slope-flat-00.png'
	base = RIGHT
	rule {
		altDelta(RIGHT, [TOP], 0) && altDelta(RIGHT, [BOTTOM, LEFT], +1)
	}
	ext.water = [ TOP, RIGHT ]
}
tile {
	id = 'water-slope-flat-northwest'
	filename = 'water-slope-flat-01.png'
	base = TOP
	rule {
		altDelta(LEFT, [TOP], 0) && altDelta(LEFT, [BOTTOM, RIGHT], +1)
	}
	ext.water = [ TOP, LEFT ]
}
tile {
	id = 'water-slope-flat-southwest'
	filename = 'water-slope-flat-02.png'
	base = LEFT
	rule {
		altDelta(LEFT, [BOTTOM], 0) && altDelta(LEFT, [TOP, RIGHT], +1)
	}
	ext.water = [ BOTTOM, LEFT ]
}
tile {
	id = 'water-slope-flat-southeast'
	filename = 'water-slope-flat-03.png'
	base = BOTTOM
	rule {
		altDelta(RIGHT, [BOTTOM], 0) && altDelta(RIGHT, [TOP, LEFT], +1)
	}
	ext.water = [ BOTTOM, RIGHT ]
}

tile {
	id = 'water-slope-flat-northeast-left'
	filename = 'water-slope-flat-left-00.png'
	base = RIGHT
	rule {
		altDelta(RIGHT, [TOP], 0) && altDelta(RIGHT, [BOTTOM, LEFT], +1)
	}
	ext.water = [ RIGHT ]
}
tile {
	id = 'water-slope-flat-northeast-right'
	filename = 'water-slope-flat-right-00.png'
	base = TOP
	rule {
		altDelta(TOP, [RIGHT], 0) && altDelta(TOP, [BOTTOM, LEFT], +1)
	}
	ext.water = [ TOP ]
}

tile {
	id = 'water-slope-flat-northwest-left'
	filename = 'water-slope-flat-left-01.png'
	base = TOP
	rule {
		altDelta(TOP, [LEFT], 0) && altDelta(TOP, [RIGHT, BOTTOM], +1)
	}
	ext.water = [ TOP ]
}
tile {
	id = 'water-slope-flat-northwest-right'
	filename = 'water-slope-flat-right-01.png'
	base = LEFT
	rule {
		altDelta(LEFT, [TOP], 0) && altDelta(LEFT, [RIGHT, BOTTOM], +1)
	}
	ext.water = [ LEFT ]
}

tile {
	id = 'water-slope-flat-southwest-left'
	filename = 'water-slope-flat-left-02.png'
	base = LEFT
	rule {
		altDelta(LEFT, [BOTTOM], 0) && altDelta(LEFT, [RIGHT, TOP], +1)
	}
	ext.water = [ LEFT ]
}
tile {
	id = 'water-slope-flat-southwest-right'
	filename = 'water-slope-flat-right-02.png'
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [LEFT], 0) && altDelta(BOTTOM, [RIGHT, TOP], +1)
	}
	ext.water = [ BOTTOM ]
}

tile {
	id = 'water-slope-flat-southeast-left'
	filename = 'water-slope-flat-left-03.png'
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [RIGHT], 0) && altDelta(BOTTOM, [LEFT, TOP], +1)
	}
	ext.water = [ BOTTOM ]
}
tile {
	id = 'water-slope-flat-southeast-right'
	filename = 'water-slope-flat-right-03.png'
	base = RIGHT
	rule {
		altDelta(RIGHT, [BOTTOM], 0) && altDelta(RIGHT, [LEFT, TOP], +1)
	}
	ext.water = [ RIGHT ]
}