
//
// Here we define the terrain tools -- raise and lower, really
//

// Give this Module a group in the tool-list
 buttonGroup 'terrain-tools', {
	title = 'Terrain Tools'
}

//
// Define a tool
tool 'terrainRaise', {
	
	button 'terrain.raise', {
		title = 'Raise terrain'
		buttonUp = 'terrain_raise_button.png'
		buttonDown = 'terrain_raise_button.png'
		group = 'terrain-tools'
	}
	
	key 'terrain.raise', {
		title = 'Raise Terrain'
		keys = 'Shift+R'
	}
	
//	active mapHover { cellX, cellY ->
//		
//	}
}

tool 'terrainLower', {
	
	button 'terrain.lower', {
		title = 'Lower terrain'
		buttonUp = 'terrain_lower_button.png'
		buttonDown = 'terrain_lower_button.png'
		group = 'terrain-tools'
	}
}