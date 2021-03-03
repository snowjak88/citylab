def heightmap = tiers {
	source = autoCorrect {
		source = fractal {
			numOctaves = 2
			type = HYBRIDMULTI
		}
		low = 0
		high = 3
		iterations = 10000
	}
	numTiers = 3
}

altitude = heightmap
tiles = { x, y -> "Grass" }