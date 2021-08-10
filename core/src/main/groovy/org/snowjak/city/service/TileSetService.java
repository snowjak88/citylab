/**
 * 
 */
package org.snowjak.city.service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.snowjak.city.CityGame;
import org.snowjak.city.map.tiles.TileSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.asset.AssetService;
import com.github.czyzby.autumn.mvc.config.AutumnActionPriority;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

/**
 * Provides Tileset-related application-level services.
 * <p>
 * Handles:
 * <ul>
 * <li>Detecting all tile-sets available at boot-time</li>
 * <li>Loading each detected tile-set at boot-time</li>
 * <li>Merging all loaded tile-sets into a single TileSet instance</li>
 * <li>Making that single TileSet instance available</li>
 * </ul>
 * </p>
 * 
 * @author snowjak88
 *
 */
@Component
public class TileSetService {
	
	private static final Logger LOG = LoggerService.forClass(TileSetService.class);
	
	@Inject
	private AssetService assetService;
	
	private final Map<String, FileHandle> tilesetScripts = new HashMap<>();
	private final Map<String, TileSet> loadedTilesets = new HashMap<>();
	
	/**
	 * Returns the list of script-names for those scripts that have already been
	 * successfully loaded.
	 * 
	 * @return
	 */
	public Set<String> getScriptNames() {
		
		return getScriptNames(true);
	}
	
	/**
	 * Returns a list of script-names. If {@code onlyLoaded}, then only returns
	 * those script-files that have been successfully loaded already.
	 * 
	 * @return
	 */
	public Set<String> getScriptNames(boolean onlyLoaded) {
		
		if (onlyLoaded) {
			//
			// Ensure that all successfully-loaded generators are populated.
			tilesetScripts.keySet().forEach(name -> {
				if (loadedTilesets.containsKey(name))
					return;
				if (!assetService.isLoaded(tilesetScripts.get(name).path()))
					return;
				loadedTilesets.put(name, assetService.get(tilesetScripts.get(name).path(), TileSet.class));
			});
			return loadedTilesets.keySet();
		}
		return tilesetScripts.keySet();
	}
	
	/**
	 * Given a TileSet instance, get the name under which we had loaded it. If a
	 * TileSet has been loaded under multiple names, picks one of them at random.
	 * 
	 * @param instance
	 * @return {@code null} if we didn't actually load this TileSet
	 */
	public String getTileSetName(TileSet instance) {
		
		return loadedTilesets.entrySet().stream().filter(e -> e.getValue() == instance).findFirst().map(Entry::getKey)
				.orElse(null);
	}
	
	/**
	 * Get the {@link TileSet} loaded under the given name. Returns {@code null} if
	 * no such TileSet was loaded.
	 * 
	 * @param name
	 * @return
	 */
	public TileSet getTileSet(String name) {
		
		if (!tilesetScripts.containsKey(name))
			return null;
		
		if (!loadedTilesets.containsKey(name))
			synchronized (this) {
				if (!loadedTilesets.containsKey(name)) {
					final TileSet tileset = assetService.finishLoading(tilesetScripts.get(name).path(), TileSet.class);
					if (tileset != null)
						loadedTilesets.put(name, tileset);
					else
						tilesetScripts.remove(name);
				}
			}
		
		return loadedTilesets.get(name);
	}
	
	@Initiate(priority = AutumnActionPriority.LOW_PRIORITY)
	public void init() {
		
		LOG.info("Initializing ...");
		
		scanDirectoryForScripts(Gdx.files.local(CityGame.EXTERNAL_ROOT_TILESETS), false).forEach(f -> {
			tilesetScripts.put(f.nameWithoutExtension(), f);
			assetService.load(f.path(), TileSet.class);
		});
		
		LOG.info("Finished initializing.");
	}
	
	private Set<FileHandle> scanDirectoryForScripts(FileHandle directory, boolean includeSubdirectories) {
		
		final Set<FileHandle> results = new LinkedHashSet<>();
		
		if (directory == null || !directory.exists())
			return results;
		
		LOG.info("Scanning [{0}]", directory.path());
		
		for (FileHandle child : directory.list())
			if (child.isDirectory() && includeSubdirectories)
				results.addAll(scanDirectoryForScripts(child, includeSubdirectories));
			else if (child.name().toLowerCase().endsWith(".groovy")) {
				LOG.info("Found script-file [{0}]", child.path());
				results.add(child);
			}
		
		return results;
	}
}
