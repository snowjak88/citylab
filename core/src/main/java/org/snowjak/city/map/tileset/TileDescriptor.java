/**
 * 
 */
package org.snowjak.city.map.tileset;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

public class TileDescriptor {
	
	public String title, id, fileName;
	public int hash, startX, startY, width, height, offset;
	public FileHandle file;
	public final Map<TileDescriptor.TileCorner, TileDescriptor.TileCornerDescriptor> corners = new HashMap<>();
	
	public enum TileCorner {
		
		NORTH(0),
		EAST(1),
		SOUTH(2),
		WEST(3);
		
		private final int index;
		
		TileCorner(int index) {
			
			this.index = index;
		}
		
		public int getIndex() {
			
			return index;
		}
		
		public static TileDescriptor.TileCorner getFor(int index) {
			
			return TileCorner.values()[index];
		}
	}
	
	public static class TileCornerDescriptor {
		
		public String id;
		public TileDescriptor ref;
		public int alt;
	}
	
	public static class DiscreteTileDescriptorSerializer implements Json.Serializer<TileDescriptor> {
		
		@Override
		public void write(Json json, TileDescriptor object, @SuppressWarnings("rawtypes") Class knownType) {
			
			//
			// We expect never to do this.
			throw new UnsupportedOperationException();
		}
		
		@Override
		public TileDescriptor read(Json json, JsonValue jsonData, @SuppressWarnings("rawtypes") Class type) {
			
			final Logger log = LoggerService.forClass(DiscreteTileDescriptorSerializer.class);
			
			if (!jsonData.has("title")) {
				log.error("Tile descriptor is malformed: no title.");
				return null;
			}
			if (!jsonData.has("id")) {
				log.error("Tile descriptor is malformed: no id.");
				return null;
			}
			if (!jsonData.has("file")) {
				log.error("Tile descriptor is malformed: no file.");
				return null;
			}
			
			final TileDescriptor td = new TileDescriptor();
			
			td.title = jsonData.getString("title");
			td.id = jsonData.getString("id");
			td.fileName = jsonData.getString("file");
			
			td.offset = jsonData.getInt("offset", 0);
			
			td.startX = 0;
			td.startY = 0;
			td.width = -1;
			td.height = -1;
			
			log.debug("Tile: ID \"{0}\", title \"{1}\", file-name \"{2}\"", td.id, td.title, td.fileName);
			
			if (jsonData.hasChild("corners")) {
				if (jsonData.get("corners").size != 4) {
					log.error("Tile descriptor is malformed: if [tiles] defines [corners], it must have 4 elements.");
					return null;
				}
				
				int j = 0;
				for (JsonValue corner : jsonData.get("corners")) {
					log.debug("Importing corner {0} ({1})", j, TileCorner.getFor(j));
					
					if (!corner.isObject()) {
						log.error("Given corner is malformed: not an object!");
						j++;
						continue;
					}
					
					if (!corner.has("ref")) {
						log.error("Given corner is malformed: no [ref]!");
						j++;
						continue;
					}
					
					final TileCornerDescriptor tcd = new TileCornerDescriptor();
					tcd.id = corner.getString("ref");
					tcd.alt = corner.getInt("alt", 0);
					
					td.corners.put(TileCorner.getFor(j), tcd);
					
					j++;
				}
			}
			
			return td;
		}
		
	}
}