def heightmap = autoCorrect {
	source = fractal { frequency = 6 }
	low = 0
	high = 3
}

altitude = heightmap
tiles = { x, y -> "Grass" }