/**
 * 
 */
package org.snowjak.city.map.tiles.support

import org.codehaus.groovy.control.CompilerConfiguration
import org.snowjak.city.map.tiles.TileSetDescriptor

/**
 * @author snowjak88
 *
 */
class TileSetDescriptorSpec  {
//	
//	public String getTitle() {
//		binding["title"]
//	}
//	public void setTitle(String title) {
//		binding["title"] = title
//	}
//	
//	public String getDescription() {
//		binding["description"]
//	}
//	public void setDescription(String description) {
//		binding["description"] = description
//	}
//	
//	public int getGridWidth() {
//		binding["gridWidth"]
//	}
//	public int setGridWidth(int gridWidth) {
//		binding["gridWidth"] = gridWidth
//	}
//	
//	public int getGridHeight() {
//		binding["gridHeight"]
//	}
//	public int setGridHeight(int gridHeight) {
//		binding["gridHeight"] = gridHeight
//	}
//	
//	public int getX() {
//		binding["x"]
//	}
//	public int setX(int x) {
//		binding["x"] = x
//	}
//	
//	public int getY() {
//		binding["y"]
//	}
//	public int setY(int y) {
//		binding["y"] = y
//	}
//	
//	public int getWidth() {
//		binding["width"]
//	}
//	public int setWidth(int width) {
//		binding["width"] = width
//	}
//	
//	public int getHeight() {
//		binding["height"]
//	}
//	public int setHeight(int height) {
//		binding["height"] = height
//	}
//	
//	public int getPadding() {
//		binding["padding"]
//	}
//	public int setPadding(int padding) {
//		binding["padding"] = padding
//	}
//	
//	public int getOffset() {
//		binding["offset"]
//	}
//	public int setOffset(int offset) {
//		binding["offset"] = offset
//	}
//	
//	public int getFilename() {
//		binding["filename"]
//	}
//	public int setFilename(String filename) {
//		binding["filename"] = filename
//	}
	
		String title = "(untitled)"
		String description = "(no description)"
	
		int gridWidth = 32
		int gridHeight = 16
	
		int x = 0, y = 0
		int width = 32, height = 32
		int padding = 0, offset = 0
		String filename = ""
	
	private List<FlavorSpec> flavors = []
	
	private int autoAdvanceLimitX = 0, autoAdvanceLimitY = 0
	private boolean autoAdvance = false
	
	private List<TileDescriptorSpec> tiles = []
	
//	
//	public abstract void scriptBody();
//	
//	
//	@Override
//	public Object run() {
//		title = "(untitled)"
//		description = "(no description)"
//		
//		gridWidth = 32
//		gridHeight = 16
//		
//		x = 0
//		y = 0
//		width = 32
//		height = 32
//		padding = 0
//		offset = 0
//		filename = ""
//		
//		scriptBody()
//	}
	
	
	
	public void tile(@DelegatesTo(TileDescriptorSpec) Closure script) {
		def tileSpec = new TileDescriptorSpec(this)
		script.resolveStrategy = Closure.DELEGATE_FIRST
		script.delegate = tileSpec
		script.owner = this
		script()
		tiles << tileSpec
		
		next()
	}
	
	public void flavor(String material, List<String> aliases) {
		
		FlavorSpec toUpdate = flavors.find { f -> f.material.equalsIgnoreCase(material) }
		
		if( toUpdate != null )
			toUpdate.aliases.addAll aliases
		else
			flavors << ( [ material: material, aliases: aliases ] as FlavorSpec )
	}
	
	public void autoAdvance(int autoAdvanceLimitX, int autoAdvanceLimitY) {
		
		if(autoAdvanceLimitX == 0 && autoAdvanceLimitY == 0) {
			this.autoAdvance = false
			return
		}
		
		this.autoAdvance = true
		this.autoAdvanceLimitX = autoAdvanceLimitX
		this.autoAdvanceLimitY = autoAdvanceLimitY
	}
	
	public void next() {
		if(autoAdvance) {
			x += (tiles.isEmpty()) ? width : tiles.last().width
			if(x >= autoAdvanceLimitX)
				nextRow()
		}
	}
	
	public void nextRow() {
		if(autoAdvance) {
			x = 0
			y += (tiles.isEmpty()) ? height : tiles.last().height
			
			if(y >= autoAdvanceLimitY)
				y = 0
		}
	}
	
	TileSetDescriptor build() {
		def tsd = new TileSetDescriptor(title, description, width, height, gridWidth, gridHeight, offset, padding)
		tiles.each { tsd.addTile it.build(tsd) }
		tsd
	}
}

class FlavorSpec {
	public String material
	public List<String> aliases = []
}