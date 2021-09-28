//
// Handles drawing an overlay of all connected network-nodes.
// This overlay is available under a specific map-mode.
//

class NetworkLegend {
	final Map<Class,String> names = [:]
	
	boolean newNetworkLegend = false
	
	void register(Class componentType, String name) {
		names[componentType] = name
		newNetworkLegend = true
	}
}

networkLegend = new NetworkLegend()
activeNetworkHighlight = null

provides networkLegend named 'networkLegend'

//
//
//

networkLegend.register Module, 'none'

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener

window 'network-legend', {
	title = i18n.get('network-legend-title')
	pin = WindowPin.BOTTOM_RIGHT
}

networkLegendCheckboxes = []
updateNetworkLegendWindow = { ->
	windows['network-legend'].clear()
	activeNetworkHighlight = null
	
	networkLegendCheckboxes.clear()
	
	networkLegend.names.each { t, n ->
		final checkbox = new CheckBox(n, skin, 'radio')
		
		checkbox.addListener( [
			changed: { event, actor ->
				if(actor.checked) {
					networkLegendCheckboxes.each { c -> c.checked = false }
					actor.checked = true
					activeNetworkHighlight = t
				}
			} ] as ChangeListener )
		checkbox.programmaticChangeEvents = false
		
		networkLegendCheckboxes << checkbox
		windows['network-legend'].newRow()
		windows['network-legend'] << checkbox
	}
	
	networkLegend.newNetworkLegend = false
}

//
//
//

isNetworkNodeMapper = ComponentMapper.getFor(IsNetworkNode)

renderHook 'network-renderer', { delta, batch, shapeDrawer, renderingSupport ->
	
	if(networkLegend.newNetworkLegend)
		updateNetworkLegendWindow()
	
	//
	// Iterate across visible map-cells ...
	final visibleCells = renderingSupport.visibleMapCells
	
	final int minX = (int) visibleCells.x, minY = (int) visibleCells.y
	final int maxX = (int)( visibleCells.x + visibleCells.width )
	final int maxY = (int)( visibleCells.y + visibleCells.height )
	for(int x=minX; x<maxX; x++)
		for(int y=minY; y<maxY; y++) {
			
			if(!state.map.isValidCell(x,y))
				continue
			
			final entity = state.map.getEntity(x,y)
			if(!isNetworkNodeMapper.has(entity))
				continue
			final networkNode = isNetworkNodeMapper.get(entity)
				
			def vertices = renderingSupport.getCellVertices( x, y, null )
			def color = Color.YELLOW
			
			if(!activeNetworkHighlight)
				continue
			
			final float middleX0 = ( vertices[0].x + vertices[1].x + vertices[2].x + vertices[3].x ) / 4.0
			final float middleY0 = ( vertices[0].y + vertices[1].y + vertices[2].y + vertices[3].y ) / 4.0
			
			for(def connection : networkNode.connections) {
				
				if(!connection.hasComponent(ComponentType.getFor(activeNetworkHighlight)))
					continue
				
				final connectionCell = isCellMapper.get(connection)
				if(!connectionCell)
					continue
				
				final int cx = connectionCell.cellX
				final int cy = connectionCell.cellY
				
				vertices = renderingSupport.getCellVertices( cx, cy, null )
				final float middleX1 = ( vertices[0].x + vertices[1].x + vertices[2].x + vertices[3].x ) / 4.0
				final float middleY1 = ( vertices[0].y + vertices[1].y + vertices[2].y + vertices[3].y ) / 4.0
				
				shapeDrawer.line middleX0, middleY0, middleX1, middleY1, color
				
			}
		}
}

mapMode 'networks', {
	title = i18n.get('mapmode-network')
	description = i18n.get('mapmode-network-description')
	includes << 'default'
	renderingHooks << 'network-renderer'
	windows << 'network-legend'
}