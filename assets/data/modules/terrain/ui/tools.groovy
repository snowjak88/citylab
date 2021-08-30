
//
// Here we define the terrain tools -- raise and lower, really
//

tool 'terrainRaise', {
			title = 'Raise terrain'
			description = 'Raise the height of a map vertex by 1 unit'
			
			buttonGroup 'terrain', {
				title = 'Terrain'
				buttonUp = 'terrain_raise_button.png'
				buttonDown = 'terrain_raise_button.png'
			}
			
			activationKey 'terrain.raise', {
				title = "Raise Terrain"
				keys = "Shift+R"
			}
			
			activationButton 'terrain.raise', {
				title = 'Raise Terrain'
				group 'terrain'
				buttonUp = 'terrain_raise_button.png'
				buttonDown = 'terrain_raise_button.png'
			}
			
//			active mapHover { cellX, cellY ->
//				//
//				// highlight nearest vertex
//			}
//			
//			active mapClick 'left', { cellX, cellY ->
//		//
//		// Adjust height of nearest vertex
//	}
}
