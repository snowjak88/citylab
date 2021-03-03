/**
 * 
 */
package org.snowjak.city.map;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import org.snowjak.city.map.TileDescriptor.CornerFilter;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;

/**
 * @author snowjak88
 *
 */
public class TileSetDescriptorSerializer implements Json.Serializer<TileSetDescriptor> {
	
	@Override
	public void write(Json json, TileSetDescriptor object, @SuppressWarnings("rawtypes") Class knownType) {
		
		//
		throw new UnsupportedOperationException(
				"Writing a TileSetDescriptor is not supported. Expected to be only read-only.");
	}
	
	@Override
	public TileSetDescriptor read(Json json, JsonValue jsonData, @SuppressWarnings("rawtypes") Class type) {
		
		if (!jsonData.isObject())
			throw new SerializationException("Cannot deserialize tile-set descriptor -- not an object.");
		
		final String title = jsonData.getString("title", "(no title)"),
				description = jsonData.getString("description", "(no description)");
		final int width = jsonData.getInt("width", -1);
		final int height = jsonData.getInt("height", -1);
		
		if (!jsonData.has("gridWidth"))
			throw new SerializationException("Cannot deserialize tile-set descriptor -- missing [gridWidth].");
		final int gridWidth = jsonData.getInt("gridWidth");
		
		if (!jsonData.has("gridHeight"))
			throw new SerializationException("Cannot deserialize tile-set descriptor -- missing [gridHeight].");
		final int gridHeight = jsonData.getInt("gridHeight");
		
		final int offset = jsonData.getInt("offset", 0);
		final int padding = jsonData.getInt("padding", 0);
		
		final TileSetDescriptor tileSet = new TileSetDescriptor(title, description, width, height, gridWidth,
				gridHeight, offset, padding);
		
		if (!jsonData.has("tiles"))
			throw new SerializationException("Cannot deserialize tile-set descriptor -- missing [tiles].");
		if (!jsonData.get("tiles").isArray())
			throw new SerializationException("Cannot deserialize tile-set descriptor -- [tiles] is not an array.");
		
		int tileIndex = 0;
		for (JsonValue tileValue : jsonData.get("tiles")) {
			try {
				
				tileSet.addTile(readTileDescriptor(tileSet, json, tileValue));
				
			} catch (SerializationException e) {
				throw new SerializationException("Cannot deserialize tile-set descriptor -- [tile][" + tileIndex
						+ "] did not deserialize properly.");
			}
			
			tileIndex++;
		}
		
		return tileSet;
	}
	
	private TileDescriptor readTileDescriptor(TileSetDescriptor tileSet, Json json, JsonValue jsonValue)
			throws SerializationException {
		
		if (!jsonValue.isObject())
			throw new SerializationException("Cannot deserialize tile descriptor -- not an object.");
		
		if (!jsonValue.has("title"))
			throw new SerializationException("Cannot deserialize tile descriptor -- [title] is required.");
		final String title = jsonValue.getString("title");
		
		if (!jsonValue.has("file"))
			throw new SerializationException("Cannot deserialize tile descriptor -- [file] is required.");
		final String filename = jsonValue.getString("file");
		
		final OptionalInt x = getOptionalInt(jsonValue, "startX");
		final OptionalInt y = getOptionalInt(jsonValue, "startY");
		final OptionalInt width = getOptionalInt(jsonValue, "width");
		final OptionalInt height = getOptionalInt(jsonValue, "height");
		final OptionalInt offset = getOptionalInt(jsonValue, "offset");
		final OptionalInt padding = getOptionalInt(jsonValue, "padding");
		
		if (!jsonValue.has("corners"))
			throw new SerializationException("Cannot deserialize tile descriptor -- [corners] is required.");
		if (!jsonValue.get("corners").isArray())
			throw new SerializationException("Cannot deserialize tile descriptor -- [corners] is not an array.");
		if (jsonValue.get("corners").size > 4)
			throw new SerializationException(
					"Cannot deserialize tile descriptor -- [corners] has more than 4 elements.");
		
		final List<CornerFilter> corners = new ArrayList<>(4);
		int cornerIndex = 0;
		for (JsonValue cornerValue : jsonValue.get("corners")) {
			
			if (!cornerValue.isObject())
				throw new SerializationException(
						"Cannot deserialize tile descriptor -- [corners][" + cornerIndex + "] is not an object.");
			
			final String cornerTileID = cornerValue.getString("ref", null);
			final OptionalInt cornerTileAltitudeDelta = getOptionalInt(cornerValue, "alt");
			corners.add(new CornerFilter(cornerTileAltitudeDelta, cornerTileID));
			
			cornerIndex++;
		}
		
		if (corners.isEmpty())
			return new TileDescriptor(tileSet, title, filename, x, y, width, height, padding, offset);
		
		return new TileDescriptor(tileSet, title, filename, x, y, width, height, padding, offset,
				corners.toArray(new CornerFilter[0]));
	}
	
	private OptionalInt getOptionalInt(JsonValue jsonValue, String name) {
		
		return jsonValue.has(name) ? OptionalInt.of(jsonValue.getInt(name)) : OptionalInt.empty();
	}
}
