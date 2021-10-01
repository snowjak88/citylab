transparent = false

//
//
//

tile {
	id = 'road-4way-grass'
	filename = 'landscapeTiles_090.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [NORTH, EAST, SOUTH, WEST] ) }
}

tile {
	id = 'road-4way-pavement'
	filename = 'cityTiles_082.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [NORTH, EAST, SOUTH, WEST] ) }
}

//
//
//

tile {
	id = 'road-t-north-grass'
	filename = 'landscapeTiles_104.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [NORTH, EAST, WEST] ) }
}

tile {
	id = 'road-t-east-grass'
	filename = 'landscapeTiles_096.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [NORTH, EAST, SOUTH] ) }
}

tile {
	id = 'road-t-south-grass'
	filename = 'landscapeTiles_097.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [EAST, SOUTH, WEST] ) }
}

tile {
	id = 'road-t-west-grass'
	filename = 'landscapeTiles_089.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [NORTH, SOUTH, WEST] ) }
}

tile {
	id = 'road-t-north-pavement'
	filename = 'cityTiles_103.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [NORTH, EAST, WEST] ) }
}

tile {
	id = 'road-t-east-pavement'
	filename = 'cityTiles_095.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [NORTH, EAST, SOUTH] ) }
}

tile {
	id = 'road-t-south-pavement'
	filename = 'cityTiles_096.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [EAST, SOUTH, WEST] ) }
}

tile {
	id = 'road-t-west-pavement'
	filename = 'cityTiles_088.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [NORTH, SOUTH, WEST] ) }
}

//
//
//

tile {
	id = 'road-straight-top-grass'
	filename = 'landscapeTiles_127.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [NORTH, WEST] ) }
}

tile {
	id = 'road-straight-right-grass'
	filename = 'landscapeTiles_125.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [NORTH, EAST] ) }
}

tile {
	id = 'road-straight-bottom-grass'
	filename = 'landscapeTiles_123.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [EAST, SOUTH] ) }
}

tile {
	id = 'road-straight-left-grass'
	filename = 'landscapeTiles_126.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [SOUTH, WEST] ) }
}

tile {
	id = 'road-straight-top-pavement'
	filename = 'cityTiles_126.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [NORTH, WEST] ) }
}

tile {
	id = 'road-straight-right-pavement'
	filename = 'cityTiles_124.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [NORTH, EAST] ) }
}

tile {
	id = 'road-straight-bottom-pavement'
	filename = 'cityTiles_122.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [EAST, SOUTH] ) }
}

tile {
	id = 'road-straight-left-pavement'
	filename = 'cityTiles_125.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [SOUTH, WEST] ) }
}

//
//
//

tile {
	id = 'road-straight-ns-grass'
	filename = 'landscapeTiles_082.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [NORTH, SOUTH] ) }
}

tile {
	id = 'road-straight-ew-grass'
	filename = 'landscapeTiles_074.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [EAST, WEST] ) }
}

tile {
	id = 'road-straight-ns-pavement'
	filename = 'cityTiles_081.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [NORTH, SOUTH] ) }
}

tile {
	id = 'road-straight-ew-pavement'
	filename = 'cityTiles_073.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [EAST, WEST] ) }
}

//
//
//

tile {
	id = 'road-straight-slope-north-grass'
	filename = 'landscapeTiles_109.png'
	base = LEFT
	rule { !ext.water }
	rule { altDelta(LEFT, [BOTTOM], 0) && altDelta(LEFT, [TOP,RIGHT], +1) }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [NORTH, SOUTH] ) }
}

tile {
	id = 'road-straight-slope-north-pavement'
	filename = 'cityTiles_108.png'
	base = LEFT
	rule { !ext.water }
	rule { altDelta(LEFT, [BOTTOM], 0) && altDelta(LEFT, [TOP,RIGHT], +1) }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [NORTH, SOUTH] ) }
}

tile {
	id = 'road-straight-slope-east-grass'
	filename = 'landscapeTiles_103.png'
	rule { !ext.water }
	rule { altDelta(LEFT, [TOP], 0) && altDelta(LEFT, [BOTTOM,RIGHT], +1) }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [EAST, WEST] ) }
}

tile {
	id = 'road-straight-slope-east-pavement'
	filename = 'cityTiles_102.png'
	rule { !ext.water }
	rule { altDelta(LEFT, [TOP], 0) && altDelta(LEFT, [BOTTOM,RIGHT], +1) }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [EAST, WEST] ) }
}

tile {
	id = 'road-straight-slope-south-grass'
	filename = 'landscapeTiles_110.png'
	rule { !ext.water }
	rule { altDelta(RIGHT, [TOP], 0) && altDelta(RIGHT, [BOTTOM,LEFT], +1) }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [NORTH, SOUTH] ) }
}

tile {
	id = 'road-straight-slope-south-pavement'
	filename = 'cityTiles_109.png'
	rule { !ext.water }
	rule { altDelta(RIGHT, [TOP], 0) && altDelta(RIGHT, [BOTTOM,LEFT], +1) }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [NORTH, SOUTH] ) }
}

tile {
	id = 'road-straight-slope-west-grass'
	filename = 'landscapeTiles_115.png'
	base = RIGHT
	rule { !ext.water }
	rule { altDelta(RIGHT, [BOTTOM], 0) && altDelta(RIGHT, [TOP,LEFT], +1) }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [EAST, WEST] ) }
}

tile {
	id = 'road-straight-slope-west-pavement'
	filename = 'cityTiles_114.png'
	base = RIGHT
	rule { !ext.water }
	rule { altDelta(RIGHT, [BOTTOM], 0) && altDelta(RIGHT, [TOP,LEFT], +1) }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [EAST, WEST] ) }
}

//
//
//

tile {
	id = 'road-end-west-grass'
	filename = 'landscapeTiles_105.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [EAST] ) }
}

tile {
	id = 'road-end-west-pavement'
	filename = 'cityTiles_104.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [EAST] ) }
}

tile {
	id = 'road-end-north-grass'
	filename = 'landscapeTiles_111.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [SOUTH] ) }
}

tile {
	id = 'road-end-north-pavement'
	filename = 'cityTiles_110.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [SOUTH] ) }
}

tile {
	id = 'road-end-east-grass'
	filename = 'landscapeTiles_112.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [WEST] ) }
}

tile {
	id = 'road-end-east-pavement'
	filename = 'cityTiles_111.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [WEST] ) }
}

tile {
	id = 'road-end-south-grass'
	filename = 'landscapeTiles_117.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'grass' }
	rule { listsMatch( ext.road, [NORTH] ) }
}

tile {
	id = 'road-end-south-pavement'
	filename = 'cityTiles_116.png'
	rule { !ext.water }
	rule { isFlat() }
	rule { ext.terrain == 'pavement' }
	rule { listsMatch( ext.road, [NORTH] ) }
}