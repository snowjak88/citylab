/**
 * 
 */
package org.snowjak.city.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.snowjak.city.CityGame;
import org.snowjak.city.map.generator.support.MapGeneratorSpec;

import com.badlogic.gdx.files.FileHandle;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.asset.AssetService;
import com.github.czyzby.autumn.mvc.config.AutumnActionPriority;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

/**
 * Provides access by name to {@link MapGeneratorSpec map-generator scripts}.
 * 
 * @author snowjak88
 *
 */
@Component
public class MapGeneratorService {
	
	private static final Logger LOG = LoggerService.forClass(MapGeneratorService.class);
	
	private final Map<String, FileHandle> scriptFiles = new LinkedHashMap<>();
	private final Map<String, MapGeneratorSpec> scripts = Collections.synchronizedMap(new LinkedHashMap<>());
	
	@Inject
	private AssetService assetService;
	
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
		
		if (onlyLoaded)
			return scripts.keySet();
		return scriptFiles.keySet();
	}
	
	/**
	 * Get the {@link MapGeneratorSpec} associated with the given name. If
	 * necessary, blocks until the named script has finished loading. If no such
	 * script associated with the given name has been successfully loaded, returns
	 * {@code null}.
	 * 
	 * @param name
	 * @return
	 */
	public MapGeneratorSpec getScript(String name) {
		
		if (!scriptFiles.containsKey(name))
			return null;
		
		return scripts.computeIfAbsent(name, (n) -> {
			assetService.finishLoading(scriptFiles.get(n).path(), MapGeneratorSpec.class);
			return assetService.get(scriptFiles.get(n).path(), MapGeneratorSpec.class);
		});
	}
	
	/**
	 * Register a {@link MapGeneratorSpec} (identified by {@code scriptFile}), and
	 * schedule it for loading with the application's {@link AssetService}. The
	 * script will be registered under the name of
	 * {@link FileHandle#nameWithoutExtension() its name without its extension}.
	 * 
	 * @param scriptFile
	 */
	public void registerScript(FileHandle scriptFile) {
		
		registerScript(scriptFile.nameWithoutExtension(), scriptFile);
	}
	
	/**
	 * Register a {@link MapGeneratorSpec} (identified by {@code scriptFile})
	 * under the given {@code scriptName}, and schedule it for loading with the
	 * application's {@link AssetService}.
	 * 
	 * @param scriptName
	 * @param scriptFile
	 */
	public void registerScript(String scriptName, FileHandle scriptFile) {
		
		final String scriptFilePath = scriptFiles.get(scriptName).path();
		
		LOG.info("Scheduling load for script-file \"{0}\" <=> [{1}]", scriptName, scriptFilePath);
		assetService.load(scriptFilePath, MapGeneratorSpec.class);
	}
	
	@Initiate(priority = AutumnActionPriority.LOW_PRIORITY)
	public void init() {
		
		LOG.info("Initializing ...");
		final FileHandle mapGeneratorRoot = CityGame.RESOLVER.resolve(CityGame.EXTERNAL_ROOT_MAP_GENERATORS);
		
		if (!mapGeneratorRoot.exists())
			mapGeneratorRoot.mkdirs();
		
		LOG.info("Scanning [{0}] for map-generator .groovy scripts ...", CityGame.EXTERNAL_ROOT_MAP_GENERATORS);
		scanDirectoryForScripts(mapGeneratorRoot);
		LOG.debug("Finished scanning for scripts.");
		
		scriptFiles.forEach(this::registerScript);
		
		LOG.info("Finished initializing.");
	}
	
	private void scanDirectoryForScripts(FileHandle directory) {
		
		if (directory == null || !directory.exists())
			return;
		
		LOG.info("Scanning [{0}]", directory.path());
		
		for (FileHandle child : directory.list())
			if (child.isDirectory())
				scanDirectoryForScripts(child);
			else if (child.name().toLowerCase().endsWith(".groovy")) {
				LOG.info("Found script-file [{0}]", child.path());
				scriptFiles.put(child.nameWithoutExtension(), child);
			}
	}
}
