/**
 * 
 */
package org.snowjak.city.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.snowjak.city.CityGame;
import org.snowjak.city.configuration.InitPriority;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.kiwi.log.Logger;
import com.kotcrab.vis.usl.USL;

/**
 * Serves as a repository for {@link Skin} instances.
 * 
 * @author snowjak88
 *
 */
@Component
public class SkinService {
	
	private static final Logger LOG = LoggerService.forClass(SkinService.class);
	
	@Inject
	private GameAssetService assetService;
	
	private final Map<String, Skin> skins = new LinkedHashMap<>();
	private final Map<String, FileHandle> loadedSkins = new LinkedHashMap<>();
	
	public void addSkin(String skinName, Skin skin) {
		
		synchronized (this) {
			skins.put(skinName, skin);
		}
	}
	
	public Skin getSkin(String skinName) {
		
		synchronized (this) {
			
			if (!skins.containsKey(skinName) && loadedSkins.containsKey(skinName)) {
				
				final FileHandle loadedSkin = loadedSkins.get(skinName);
				
				assetService.finishLoading(loadedSkin.path());
				skins.put(skinName, assetService.get(loadedSkin.path(), Skin.class));
			}
			
			return skins.get(skinName);
		}
	}
	
	@Initiate(priority = InitPriority.HIGH_PRIORITY)
	public void init() {
		
		LOG.info("Initializing ...");
		
		scanForSkins(CityGame.RESOLVER.resolve(CityGame.INTERNAL_SKIN_BASE));
		
		LOG.info("Finished initialization.");
	}
	
	private void scanForSkins(FileHandle baseDirectory) {
		
		if (baseDirectory == null || !baseDirectory.exists() || !baseDirectory.isDirectory())
			return;
			
		//
		// Look for sub-directories that contain a file,
		// named either <subdirectory>.json or <subdirectory>.usl
		//
		
		LOG.info("Scanning for skin-files in {0}", baseDirectory.path());
		
		for (FileHandle subdirectory : baseDirectory.list()) {
			
			if (!subdirectory.isDirectory())
				continue;
			
			final String skinName = subdirectory.name();
			final FileHandle jsonFile = subdirectory.child(skinName + ".json"),
					uslFile = subdirectory.child(skinName + ".usl");
			
			if (jsonFile.exists()) {
				LOG.info("Loading skin \"{0}\" from [{1}].", skinName, jsonFile.path());
				
				assetService.load(jsonFile.path(), Skin.class);
				loadedSkins.put(skinName, jsonFile);
				
			} else if (uslFile.exists()) {
				LOG.info("Compiling skin \"{0}\" from [{1}]", skinName, uslFile.path());
				
				final FileHandle tempDirectory = FileHandle.tempDirectory("skin_compile_" + skinName);
				final FileHandle tempJson = tempDirectory.child(skinName + ".json");
				
				LOG.info("Copying dependencies to compile-directory ...");
				for (FileHandle child : subdirectory.list())
					child.copyTo(tempDirectory);
				
				LOG.info("Compiling skin to JSON ...");
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempJson.file()))) {
					bw.write(USL.parse(tempDirectory.file(), uslFile.readString()));
				} catch (IOException e) {
					LOG.error(e, "Cannot compile skin \"{0}\" from [{1}]", skinName, uslFile.path());
				}
				
				LOG.info("Loading compiled skin \"{0}\".", skinName);
				assetService.load(tempJson.path(), Skin.class);
				loadedSkins.put(skinName, tempJson);
				
				assetService.addOnLoadAction(() -> tempDirectory.deleteDirectory());
			}
		}
	}
}
