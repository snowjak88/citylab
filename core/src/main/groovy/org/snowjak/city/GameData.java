/**
 * 
 */
package org.snowjak.city;

import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.map.tiles.TileSet;

/**
 * Encapsulates game data.
 * 
 * @author snowjak88
 *
 */
public class GameData {
	
	private static GameData __instance = null;
	
	public static GameData get() {
		
		if (__instance == null)
			synchronized (GameData.class) {
				if (__instance == null)
					__instance = new GameData();
			}
		return __instance;
	}
	
	/**
	 * Seed to be used for random-number generation.
	 */
	public String seed = Long.toString(System.currentTimeMillis());
	
	/**
	 * The current {@link CityMap}.
	 */
	public CityMap map = null;
	
	/**
	 * When we switch to the game screen, we'll use these configured parameters.
	 */
	public GameParameters parameters = new GameParameters();
	
	public static class GameParameters {
		
		public int mapWidth = 64, mapHeight = 64;
		
		public String seed = "";
		
		public String selectedMapGeneratorName = "rolling-hills";
		public MapGenerator selectedMapGenerator;
		
		public String selectedTilesetName = "default";
		public TileSet selectedTileset;
	}
}
