//
// Define some landscape/terrain tiles for the including tile-set.
//

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
		alt(TOP) == alt(RIGHT) && altDelta(TOP, [BOTTOM, LEFT], +1)
	}
}

tile {
	id = 'grass-slope-east'
	filename = 'grass-slope01.png'
	provides 'grass'
	rule {
		alt(TOP) == alt(LEFT) && altDelta(TOP, [BOTTOM, RIGHT], +1)
	}
}

tile {
	id = 'grass-slope-north'
	filename = 'grass-slope02.png'
	provides 'grass'
	base = LEFT
	rule {
		alt(LEFT) == alt(BOTTOM) && altDelta(LEFT, [TOP, RIGHT], +1)
	}
}

tile {
	id = 'grass-slope-west'
	filename = 'grass-slope03.png'
	provides 'grass'
	base = RIGHT
	rule {
		alt(RIGHT) == alt(BOTTOM) && altDelta(RIGHT, [TOP, LEFT], +1)
	}
}

tile {
	id = 'grass-slope-concave-south'
	filename = 'grass-slope-concave00.png'
	provides 'grass'
	rule {
		altDelta(TOP, [RIGHT, BOTTOM, LEFT], +1)
	}
}

tile {
	id = 'grass-slope-concave-east'
	filename = 'grass-slope-concave01.png'
	provides 'grass'
	base = LEFT
	rule {
		altDelta(LEFT, [RIGHT, BOTTOM, TOP], +1)
	}
}

tile {
	id = 'grass-slope-concave-north'
	filename = 'grass-slope-concave02.png'
	provides 'grass'
	base = BOTTOM
	rule {
		altDelta(BOTTOM, [RIGHT, TOP, LEFT], +1)
	}
}

tile {
	id = 'grass-slope-concave-west'
	filename = 'grass-slope-concave03.png'
	provides 'grass'
	base = RIGHT
	rule {
		altDelta(RIGHT, [TOP, BOTTOM, LEFT], +1)
	}
}

tile {
	id = 'grass-slope-convex-west'
	filename = 'grass-slope-convex00.png'
	provides 'grass'
	rule {
		altDelta(LEFT, [TOP, BOTTOM, RIGHT], -1)
	}
}

tile {
	id = 'grass-slope-convex-south'
	filename = 'grass-slope-convex01.png'
	provides 'grass'
	rule {
		altDelta(BOTTOM, [TOP, LEFT, RIGHT], -1)
	}
}

tile {
	id = 'grass-slope-convex-east'
	filename = 'grass-slope-convex02.png'
	provides 'grass'
	rule {
		altDelta(RIGHT, [TOP, BOTTOM, LEFT], -1)
	}
}

tile {
	id = 'grass-slope-convex-north'
	filename = 'grass-slope-convex03.png'
	provides 'grass'
	base = RIGHT
	rule {
		altDelta(TOP, [RIGHT, BOTTOM, LEFT], -1)
	}
}