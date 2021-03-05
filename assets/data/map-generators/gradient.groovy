def heightmap = autoCorrect {
	source = gradient {
		extremes = [ 0, 8, 0, 0 ]
		axis = Y_AXIS
	}
	low = 1
	high = 2
}

altitude = heightmap
