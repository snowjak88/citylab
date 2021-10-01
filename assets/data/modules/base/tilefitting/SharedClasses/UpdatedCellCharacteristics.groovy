import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

import org.snowjak.city.map.tiles.TileSet

import groovy.util.Expando

class UpdatedCellCharacteristics implements Component, Poolable {
	
	String layerID
	TileSet tileset
	final int[][] heights = new int[2][2]
	Integer altitudeOverride
	boolean searchSeparately = false
	final Expando ext = new Expando()
	
	void reset() {
		layerID = null
		tileset = null
		heights[0][0] = 0
		heights[0][1] = 0
		heights[1][0] = 0
		heights[1][1] = 0
		altitudeOverride = null
		searchSeparately = false
		ext.properties.clear()
	}
	
}