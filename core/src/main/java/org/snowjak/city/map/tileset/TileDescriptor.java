/**
 * 
 */
package org.snowjak.city.map.tileset;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.files.FileHandle;

public class TileDescriptor {
	
	public String title, stringID, fileName;
	public int hash, startX, startY, width, height;
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
}