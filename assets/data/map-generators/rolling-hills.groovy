def lowest = 0
def highest = 4

def heightmap = scaleDomain {
	source = autoCorrect {
		source = fractal {
			numOctaves = 2
			seed = rnd() * 1024
		}
		low = lowest
		high = highest
		iterations = 10000
	}
	scale = 1.0/64.0
}

altitude = heightmap
flavors = { x,y ->
	['grass']
}