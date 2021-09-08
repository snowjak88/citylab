
decoration = true
surfaceOffset = 0

tile {
	id = 'water-flat-west'
	filename = 'water-flat-00.png'
	provides [LEFT], ['water']
	base = LEFT
	rule {
		altDelta(LEFT, [TOP, BOTTOM], 0) && alt(LEFT) <= alt(RIGHT)
	}
}
tile {
	id = 'water-flat-south'
	filename = 'water-flat-01.png'
	provides [BOTTOM], ['water']
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [RIGHT, LEFT], 0) && alt(BOTTOM) <= alt(TOP)
	}
}
tile {
	id = 'water-flat-east'
	filename = 'water-flat-02.png'
	provides [RIGHT], ['water']
	base = RIGHT
	rule {
		altDelta(RIGHT, [TOP, BOTTOM], 0) && alt(RIGHT) <= alt(LEFT)
	}
}
tile {
	id = 'water-flat-north'
	filename = 'water-flat-03.png'
	provides [TOP], ['water']
	base = TOP
	rule {
		altDelta(TOP, [RIGHT, LEFT], 0) && alt(TOP) <= alt(BOTTOM)
	}
}

//
//
//

tile {
	id = 'water-saddle-flat-south'
	filename = 'water-saddle-flat-00.png'
	provides [BOTTOM], ['water']
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [TOP], 0) && altDelta(BOTTOM, [RIGHT,LEFT], +1)
	}
}
tile {
	id = 'water-saddle-flat-east'
	filename = 'water-saddle-flat-01.png'
	provides [RIGHT], ['water']
	base = RIGHT
	rule {
		altDelta(RIGHT, [LEFT], 0) && altDelta(RIGHT, [TOP,BOTTOM], +1)
	}
}
tile {
	id = 'water-saddle-flat-north'
	filename = 'water-saddle-flat-02.png'
	provides [TOP], ['water']
	base = TOP
	rule {
		altDelta(TOP, [BOTTOM], 0) && altDelta(TOP, [RIGHT,LEFT], +1)
	}
}
tile {
	id = 'water-saddle-flat-west'
	filename = 'water-saddle-flat-03.png'
	provides [LEFT], ['water']
	base = LEFT
	rule {
		altDelta(LEFT, [RIGHT], 0) && altDelta(LEFT, [TOP,BOTTOM], +1)
	}
}

//
//
//

tile {
	id = 'water-slope-concave-flat-north'
	filename = 'water-slope-concave-flat-00.png'
	provides [TOP], ['water']
	base = TOP
	rule {
		altDelta(TOP, [RIGHT, BOTTOM, LEFT], +1)
	}
}
tile {
	id = 'water-slope-concave-flat-west'
	filename = 'water-slope-concave-flat-01.png'
	provides [LEFT], ['water']
	base = LEFT
	rule {
		altDelta(LEFT, [RIGHT, BOTTOM, TOP], +1)
	}
}
tile {
	id = 'water-slope-concave-flat-south'
	filename = 'water-slope-concave-flat-02.png'
	provides [TOP], ['water']
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [RIGHT, TOP, LEFT], +1)
	}
}
tile {
	id = 'water-slope-concave-flat-east'
	filename = 'water-slope-concave-flat-03.png'
	provides [RIGHT], ['water']
	base = RIGHT
	rule {
		altDelta(RIGHT, [LEFT, BOTTOM, TOP], +1)
	}
}

//
//
//

tile {
	id = 'water-slope-convex-flat-east-left'
	filename = 'water-slope-convex-flat-left-00.png'
	provides [BOTTOM], ['water']
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [RIGHT, TOP], 0) && altDelta(BOTTOM, [LEFT], +1)
	}
}
tile {
	id = 'water-slope-convex-flat-north-left'
	filename = 'water-slope-convex-flat-left-01.png'
	provides [RIGHT], ['water']
	base = RIGHT
	rule {
		altDelta(RIGHT, [TOP, LEFT], 0) && altDelta(RIGHT, [BOTTOM], +1)
	}
}
tile {
	id = 'water-slope-convex-flat-west-left'
	filename = 'water-slope-convex-flat-left-02.png'
	provides [TOP], ['water']
	base = TOP
	rule {
		altDelta(TOP, [LEFT, BOTTOM], 0) && altDelta(TOP, [RIGHT], +1)
	}
}
tile {
	id = 'water-slope-convex-flat-south-left'
	filename = 'water-slope-convex-flat-left-03.png'
	provides [LEFT], ['water']
	base = LEFT
	rule {
		altDelta(LEFT, [BOTTOM, RIGHT], 0) && altDelta(LEFT, [TOP], +1)
	}
}

tile {
	id = 'water-slope-convex-flat-east-right'
	filename = 'water-slope-convex-flat-right-00.png'
	provides [TOP], ['water']
	base = TOP
	rule {
		altDelta(TOP, [RIGHT, BOTTOM], 0) && altDelta(TOP, [LEFT], +1)
	}
}
tile {
	id = 'water-slope-convex-flat-north-right'
	filename = 'water-slope-convex-flat-right-01.png'
	provides [LEFT], ['water']
	base = LEFT
	rule {
		altDelta(LEFT, [TOP, RIGHT], 0) && altDelta(LEFT, [BOTTOM], +1)
	}
}
tile {
	id = 'water-slope-convex-flat-west-right'
	filename = 'water-slope-convex-flat-right-02.png'
	provides [BOTTOM], ['water']
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [LEFT, TOP], 0) && altDelta(BOTTOM, [RIGHT], +1)
	}
}
tile {
	id = 'water-slope-convex-flat-south-right'
	filename = 'water-slope-convex-flat-right-03.png'
	provides [RIGHT], ['water']
	base = RIGHT
	rule {
		altDelta(RIGHT, [BOTTOM, LEFT], 0) && altDelta(RIGHT, [TOP], +1)
	}
}

//
//
//

tile {
	id = 'water-slope-flat-northeast-left'
	filename = 'water-slope-flat-left-00.png'
	provides [RIGHT], ['water']
	base = RIGHT
	rule {
		altDelta(RIGHT, [TOP], 0) && altDelta(RIGHT, [BOTTOM, LEFT], +1)
	}
}
tile {
	id = 'water-slope-flat-northeast-right'
	filename = 'water-slope-flat-right-00.png'
	provides [TOP], ['water']
	base = TOP
	rule {
		altDelta(TOP, [RIGHT], 0) && altDelta(TOP, [BOTTOM, LEFT], +1)
	}
}

tile {
	id = 'water-slope-flat-northwest-left'
	filename = 'water-slope-flat-left-01.png'
	provides [TOP], ['water']
	base = TOP
	rule {
		altDelta(TOP, [LEFT], 0) && altDelta(TOP, [RIGHT, BOTTOM], +1)
	}
}
tile {
	id = 'water-slope-flat-northwest-right'
	filename = 'water-slope-flat-right-01.png'
	provides [LEFT], ['water']
	base = LEFT
	rule {
		altDelta(LEFT, [TOP], 0) && altDelta(LEFT, [RIGHT, BOTTOM], +1)
	}
}

tile {
	id = 'water-slope-flat-southwest-left'
	filename = 'water-slope-flat-left-02.png'
	provides [LEFT], ['water']
	base = LEFT
	rule {
		altDelta(LEFT, [BOTTOM], 0) && altDelta(LEFT, [RIGHT, TOP], +1)
	}
}
tile {
	id = 'water-slope-flat-southwest-right'
	filename = 'water-slope-flat-right-02.png'
	provides [BOTTOM], ['water']
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [LEFT], 0) && altDelta(BOTTOM, [RIGHT, TOP], +1)
	}
}

tile {
	id = 'water-slope-flat-southeast-left'
	filename = 'water-slope-flat-left-03.png'
	provides [BOTTOM], ['water']
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [RIGHT], 0) && altDelta(BOTTOM, [LEFT, TOP], +1)
	}
}
tile {
	id = 'water-slope-flat-southeast-right'
	filename = 'water-slope-flat-right-03.png'
	provides [RIGHT], ['water']
	base = RIGHT
	rule {
		altDelta(RIGHT, [BOTTOM], 0) && altDelta(RIGHT, [LEFT, TOP], +1)
	}
}