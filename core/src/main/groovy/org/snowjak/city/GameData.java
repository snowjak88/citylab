/**
 * 
 */
package org.snowjak.city;

import java.time.Duration;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.map.renderer.AbstractMapRenderingHook;

import com.badlogic.ashley.core.Engine;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

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
	 * Thread-caching {@link ExecutorService}.
	 */
	public final ListeningExecutorService executor = MoreExecutors.listeningDecorator(MoreExecutors
			.getExitingExecutorService((ThreadPoolExecutor) Executors.newCachedThreadPool(), Duration.ofSeconds(5)));
	
	/**
	 * Hooks into the {@link CityMap}-rendering process.
	 */
	public final SortedSet<AbstractMapRenderingHook> mapRenderingHooks = new TreeSet<AbstractMapRenderingHook>(
			(h1, h2) -> Integer.compare(h1.getOrder(), h2.getOrder()));
	
	/**
	 * Seed to be used for random-number generation.
	 */
	public String seed = Long.toString(System.currentTimeMillis());
	
	/**
	 * The current {@link CityMap}.
	 */
	public CityMap map = null;
	
	/**
	 * The current entity-processing {@link Engine}.
	 */
	public Engine entityEngine = null;
	
	/**
	 * Parameters to be used when getting ready to play the game.
	 */
	public GameParameters parameters = new GameParameters();
	
	public static class GameParameters {
		
		public int mapWidth = 64, mapHeight = 64;
		
		public String seed = "";
		
		public String selectedMapGeneratorName = "rolling-hills";
		public MapGenerator selectedMapGenerator;
	}
}
