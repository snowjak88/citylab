def heightmap = tiers {
	source = autoCorrect {
		source = fractal {
			numOctaves = 5
		}
		low = 0
		high = 3
		iterations = 10000
	}
	numTiers = 3
}

altitude = heightmap
tiles = { x, y -> "Grass" }