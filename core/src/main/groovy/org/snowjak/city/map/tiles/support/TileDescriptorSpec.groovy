/**
 * 
 */
package org.snowjak.city.map.tiles.support

import org.snowjak.city.map.tiles.TileDescriptor
import org.snowjak.city.map.tiles.TileSetDescriptor
import org.snowjak.city.map.tiles.TileDescriptor.Corner
import org.snowjak.city.map.tiles.TileDescriptor.CornerFilter

/**
 * @author snowjak88
 *
 */
class TileDescriptorSpec {
	private TileSetDescriptorSpec tsd
	
	public String id
	public int x, y
	public int width, height
	public int padding, offset
	public String filename
	
	private List<CornerFilterSpec> corners = new ArrayList<>()
	
	TileDescriptorSpec(TileSetDescriptorSpec tsd) {
		this.tsd = tsd
		x = tsd.x
		y = tsd.y
		width = tsd.width
		height = tsd.height
		filename = tsd.filename
		padding = tsd.padding
		offset = tsd.offset
	}
	
	public TileDescriptor build(TileSetDescriptor tileSetDescriptor) {
		
		def cornerFilters = new ArrayList<CornerFilter>()
		
		this.corners.forEach { cfs ->
			//
			// Create a new CornerFilter for this CornerFilterSpec
			//
			// To do that, we need to translate the set of "flavors"
			// into material-IDs and aliases
			//
			def materials = new HashMap<String,Set<String>>()
			
			//
			// Look at each "flavor" in turn --
			cfs.flavors.each { f ->
				//
				// Look up the material-name corresponding to this alias
				def material = tsd.flavors.find {
					it.aliases.findIndexOf {
						it == f
					} >= 0
				}
				
				//
				// If we declared no such alias, assume it's a material-ID
				if(material == null)
					materials.put f, new LinkedHashSet<>()
				
				else
					//
					// Add the computed material-name and its alias to the CornerFilter materials map
					materials.computeIfAbsent(material.material, {
						new LinkedHashSet<>()
					}).add f
				
			}
			
			//
			// Build the new CornerFilter
			cornerFilters << new CornerFilter((cfs.altitudeDelta == null) ? OptionalInt.empty() : OptionalInt.of(cfs.altitudeDelta.intValue()), materials)
		}
		
//		if(!cornerFilters.isEmpty()) {
//			final OptionalInt rootAltitude = cornerFilters[Corner.getForDelta(-1, -1).ordinal()].altitudeDelta
//			if(rootAltitude.isPresent())
//				for(def i in 1..cornerFilters.size()-1)
//					if(cornerFilters[i].altitudeDelta.isPresent()) {
//						def newAltitudeDelta = OptionalInt.of( cornerFilters[i].altitudeDelta.getAsInt() - rootAltitude.getAsInt() )
//						cornerFilters[i] = new CornerFilter(newAltitudeDelta, cornerFilters[i].flavors)
//					}
//		}
		
		def td = new TileDescriptor(tileSetDescriptor, id, filename, x, y, width, height, padding, offset, cornerFilters.toArray(new CornerFilter[0]));
	}
	
	public void corner(List<String> flavors, Integer altitudeDelta) {
		corners << ( [flavors: flavors, altitudeDelta: altitudeDelta] as CornerFilterSpec )
	}
}

class CornerFilterSpec {
	public List<String> flavors = []
	public Integer altitudeDelta = null
}