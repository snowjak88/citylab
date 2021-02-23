/**
 * 
 */
package org.snowjak.city.map.tileset;

import java.util.LinkedHashMap;
import java.util.Map;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

/**
 * @author snowjak88
 *
 */
public class TilesetDescriptor {
	
	public String title, description;
	public int baseWidth, baseHeight, baseOffset, padding;
	public TilesetType type;
	public final Map<String, TileDescriptor> tilesById = new LinkedHashMap<>();
	public final IntMap<TileDescriptor> tilesByHash = new IntMap<>();
	
	public enum TilesetType {
		
		DISCRETE("discrete"),
		SPRITESHEET("spritesheet");
		
		private final String type;
		
		TilesetType(String type) {
			
			this.type = type;
		}
		
		public String getType() {
			
			return type;
		}
		
		/**
		 * Given a "type" text-value, return the corresponding TilesetType (or
		 * {@code null} if no configured TilesetTypes match).
		 * 
		 * @param type
		 * @return
		 */
		public static TilesetType getFor(String type) {
			
			for (int i = 0; i < TilesetType.values().length; i++)
				if (TilesetType.values()[i].type.equalsIgnoreCase(type))
					return TilesetType.values()[i];
			return null;
		}
	}
	
	public static class Serializer implements Json.Serializer<TilesetDescriptor> {
		
		@Override
		public void write(Json json, TilesetDescriptor object, @SuppressWarnings("rawtypes") Class knownType) {
			
			//
			// We expect never to do this.
			throw new UnsupportedOperationException();
		}
		
		@Override
		public TilesetDescriptor read(Json json, JsonValue jsonData, @SuppressWarnings("rawtypes") Class type) {
			
			final Logger log = LoggerService.forClass(TilesetDescriptor.Serializer.class);
			
			log.debug("Loading tile-set descriptor [{0} ...]", jsonData.toString().substring(0, 32));
			
			final TilesetDescriptor d = new TilesetDescriptor();
			
			d.title = jsonData.getString("title", "(untitled)");
			log.debug("Title: {0}", d.title);
			
			d.description = jsonData.getString("description", "(no description)");
			log.debug("Description: {0}", d.description);
			
			d.baseWidth = jsonData.getInt("baseWidth", 64);
			d.baseHeight = jsonData.getInt("baseHeight", 32);
			d.baseOffset = jsonData.getInt("baseOffset", 0);
			d.padding = jsonData.getInt("padding", 0);
			
			log.debug("Base width / height / offset / padding: {0} / {1} / {2} / {3}", d.baseWidth, d.baseHeight,
					d.baseOffset, d.padding);
			
			d.type = TilesetType.getFor(jsonData.getString("type", TilesetType.DISCRETE.type));
			log.debug("Type: {0}", d.type.getType());
			
			switch (d.type) {
			
			case DISCRETE:
				if (!jsonData.has("tiles")) {
					log.error("Tile-set descriptor is DISCRETE, but has no tiles.");
					return d;
				}
				if (!jsonData.get("tiles").isArray()) {
					log.error("Tile-set descriptor is malformed: [tiles] is not an array.");
					return d;
				}
				
				int i = 0;
				for (JsonValue tile : jsonData.get("tiles")) {
					if (!tile.isObject()) {
						log.error("Tile-set descriptor is malformed: [tiles][{0}] is not an object.", i);
						continue;
					}
					
					//
					// We manually reference the TileDescriptor de-serializer, so we can select the
					// correct one.
					final TileDescriptor td = new TileDescriptor.DiscreteTileDescriptorSerializer().read(json, tile,
							TileDescriptor.class);
					if (td == null) {
						log.error("Tile-set descriptor is malformed: [tiles][{0}] could not be parsed.", i);
						i++;
						continue;
					}
					
					d.tilesById.put(td.id, td);
					d.tilesByHash.put(td.hash, td);
					
					i++;
				}
				break;
			
			case SPRITESHEET:
				throw new UnsupportedOperationException();
			}
			
			return d;
		}
	}
}
