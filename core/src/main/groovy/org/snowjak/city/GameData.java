/**
 * 
 */
package org.snowjak.city;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.snowjak.city.map.CityMap;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.map.renderer.hooks.AbstractCellRenderingHook;
import org.snowjak.city.map.renderer.hooks.AbstractCustomRenderingHook;
import org.snowjak.city.util.RelativePriorityList;

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
	 * Hooks into the {@link CityMap}-rendering process. These are called every
	 * frame, in ascending order of their priority, for every visible map-cell.
	 * <p>
	 * Note that
	 * </p>
	 */
	public final Map<String, AbstractCellRenderingHook> cellRenderingHooks = new LinkedHashMap<>();
	
	/**
	 * Prioritized version of {@link #cellRenderingHooks}.
	 */
	public final RelativePriorityList<String, AbstractCellRenderingHook> prioritizedCellRenderingHooks = new RelativePriorityList<>();
	
	/**
	 * Hooks into the {@link CityMap}-rendering process. These are called every
	 * frame, in ascending order of their priority. The map-renderer itself has
	 * priority 0; hooks with negative priority are called before, while those with
	 * positive priority are called after.
	 */
	public final Map<String, AbstractCustomRenderingHook> customRenderingHooks = new LinkedHashMap<>();
	
	/**
	 * Prioritized version of {@link #customRenderingHooks}.
	 */
	public final RelativePriorityList<String, AbstractCustomRenderingHook> prioritizedCustomRenderingHooks = new RelativePriorityList<>();
	
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
	public Engine engine = null;
	
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
