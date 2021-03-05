def heightmap = scaleDomain {
	source = autoCorrect {
		source = fractal {
			numOctaves = 2
		}
		low = 0
		high = 4
		iterations = 10000
	}
	scale = 1.0/40.0
}

altitude = heightmap
