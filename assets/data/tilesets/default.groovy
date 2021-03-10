
	id = "default"
	title = "Default tileset"
	description = "Default world tileset. Created by darkrose (https://opengameart.org/users/darkrose)."
	
	//
	// The grid-dimensions for which this tileset can be made to work
	//
	// (Default: 32 / 16)
	gridWidth = 64
	gridHeight = 32
	
	//
	// Default width/height for all tiles
	//
	// (Default: 32 / 32)
	width = 64
	height = 64
	
	//
	// Default file-name for all tiles
	// Must be in the same directory as this descriptor
	filename = "terrain_0.png"
	
	//
	// Starting x/y
	//
	// (Default: 0 / 0)
	x = 0
	y = 0
	
	//
	// Drawing offset (along Y axis, from the tile's bottom edge)
	// i.e., how far should the tile be offset so its visual "bottom" corner
	// is aligned with the grid-cell's "bottom" corner
	//
	// (Default: 0)
	offset = 0
	
	//
	// Each tile's image has so much padding (empty space surrounding
	// the "meat" of the tile, and which should be ignored)
	//
	// (Default: 0)
	padding = 0
	
	//
	// Correlate material-IDs with "flavors" -- i.e.,
	// multiple aliases for a single material that should
	// only be matched with themselves
	//
	// Allows you to supply multiple "styles" for a single material,
	// ensuring your tiles are matched only with appropriate neighbors
	flavor "grass", [ "grass", "dirty-grass" ]
	
	//
	// "Auto-advance" x/y for every subsequent tile
	// Because [filename] is not loaded right when the script is executed,
	// you need to supply the image dimensions here
	//
	// Once set up, the "cursor" will iterate to the next tile-slot with every
	// tile definition
	//
	// You can also control the cursor by calling one of:
	//  - next()     -- skip next tile (assumed to be the same dimensions as the
	//                  previous tile, or the globally-configured width/height
	//  - nextRow()  -- skip to the beginning of the next row
	autoAdvance 640, 1024
	
	//
	// As it happens, we need to skip the very first tile-cell
	next()
	
	//
	// Define a tile
	// Inherits fields from its enclosing tile-set:
	//  - x/y
	//  - width/height
	//  - offset
	//  - padding
	//  - filename
	//
	// You may certainly override them here, if you need them to be specific
	tile {
		id = "A"
		//
		// Corner-filters clockwise from the top
		// If you have fewer than 4, the missing entries are auto-populated with "wildcard" entries
		// If you have more than 4, the excess entries are ignored
		//
		// Ordinarily, each material-ID here is assumed to be a "flavor" (i.e., one of
		// the aliases defined above)
		// You may also refer to a material-ID directly here, so long as that ID has not
		// already been defined as a flavor
		corner [ "grass" ], 0 
		corner [ "grass" ], 0 
		corner [ "grass" ], 0 
		corner [ "grass" ], 0
	}
	
	tile {
		id = "B"
		corner [ "grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], 0
	}
	
	tile {
		id = "C"
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
	}
	
	tile {
		id = "D"
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
	}
	
	tile {
		id = "E"
		corner [ "grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], 0
	}
	
	tile {
		id = "F"
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
	}
	
	tile {
		id = "G"
		corner [ "grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "grass" ], 0
	}
	
	tile {
		id = "H"
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], 0
	}
	
	tile {
		id = "I"
		corner [ "dirty-grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], 0
		corner [ "dirty-grass" ], 0
	}
	
	tile {
		id = "J"
		corner [ "grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
	}
	
	tile {
		id = "K"
		corner [ "dirty-grass" ], 0
		corner [ "grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
	}
	
	tile {
		id = "L"
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "grass" ], 0
		corner [ "dirty-grass" ], 0
	}
	
	tile {
		id = "M"
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "grass" ], 0
	}
	
	tile {
		id = "N"
		corner [ "dirty-grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], 0
	}
	
	tile {
		id = "O"
		corner [ "grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], 0
	}
	
	tile {
		id = "P"
		corner [ "grass" ], 0
		corner [ "grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "grass" ], 0
	}
	
	tile {
		id = "Q"
		corner [ "grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], 0
		corner [ "dirty-grass" ], 0
	}
	
	tile {
		id = "R"
		corner [ "grass" ], 0
		corner [ "grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
	}
	
	tile {
		id = "S"
		corner [ "grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "grass" ], 0
	}
	
	nextRow()
	next()
	next()
	next()
	next()
	
	tile {
		id = "AG"
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
		corner [ "dirty-grass" ], 0
	}
	
	offset = 32
	
	tile {
		id = "AH"
		corner [ "grass" ], -1
		corner [ "grass" ], -1
		corner [ "grass" ], -1
		corner [ "grass" ], 0
	}
	
	tile {
		id = "AI"
		corner [ "grass" ], -1
		corner [ "grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], -1
	}
	
	tile {
		id = "AJ"
		corner [ "grass" ], -1
		corner [ "grass" ], -1
		corner [ "grass" ], 0
		corner [ "grass" ], 0
	}
	
	tile {
		id = "AK"
		corner [ "grass" ], -1
		corner [ "grass" ], -1
		corner [ "grass" ], 0
		corner [ "grass" ], -1
	}
	
	nextRow()
	
	tile {
		id = "AL"
		corner [ "grass" ], -1
		corner [ "grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], 0
	}
	
	tile {
		id = "AM"
		corner [ "grass" ], 0
		corner [ "grass" ], -1
		corner [ "grass" ], 0
		corner [ "grass" ], 0
	}
	
	tile {
		id = "AN"
		corner [ "grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], -1
		corner [ "grass" ], 0
	}
	
	tile {
		id = "AO"
		corner [ "grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], -1
	}
	
	tile {
		id = "AP"
		corner [ "grass" ], 0
		corner [ "grass" ], -1
		corner [ "grass" ], -1
		corner [ "grass" ], -1
	}
	
	tile {
		id = "AQ"
		corner [ "grass" ], 0
		corner [ "grass" ], 0
		corner [ "grass" ], -1
		corner [ "grass" ], -1
	}
	
	tile {
		id = "AR"
		corner [ "grass" ], 0
		corner [ "grass" ], -1
		corner [ "grass" ], -1
		corner [ "grass" ], 0
	}
	
	tile {
		id = "AS"
		corner [ "grass" ], -1
		corner [ "grass" ], 0
		corner [ "grass" ], -1
		corner [ "grass" ], -1
	}
	
	nextRow()
