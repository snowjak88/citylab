/**
 * 
 */
package org.snowjak.city;

import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.generator.MapGenerator;

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
	 * The current {@link CityMap}.
	 */
	public CityMap map = null;
	
	/**
	 * When we switch to the game screen, we'll use these configured parameters.
	 */
	public GameParameters parameters = new GameParameters();
	
	public static class GameParameters {
		
		public int mapWidth = 64, mapHeight = 64;
		
		public String selectedMapGeneratorName = "rolling-hills";
		public MapGenerator selectedMapGenerator;
	}
}
