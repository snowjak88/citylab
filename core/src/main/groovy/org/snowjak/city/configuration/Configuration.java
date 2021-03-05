package org.snowjak.city.configuration;

import org.snowjak.city.CityGame;
import org.snowjak.city.map.TileSet;
import org.snowjak.city.map.TileSetLoader;
import org.snowjak.city.map.generator.JavaMapGeneratorLoader;
import org.snowjak.city.map.generator.support.MapGeneratorScript;
import org.snowjak.city.service.ScaleService;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.mvc.component.asset.AssetService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.SkinService;
import com.github.czyzby.autumn.mvc.stereotype.preference.AvailableLocales;
import com.github.czyzby.autumn.mvc.stereotype.preference.I18nBundle;
import com.github.czyzby.autumn.mvc.stereotype.preference.I18nLocale;
import com.github.czyzby.autumn.mvc.stereotype.preference.LmlMacro;
import com.github.czyzby.autumn.mvc.stereotype.preference.LmlParserSyntax;
import com.github.czyzby.autumn.mvc.stereotype.preference.Preference;
import com.github.czyzby.autumn.mvc.stereotype.preference.StageViewport;
import com.github.czyzby.autumn.mvc.stereotype.preference.sfx.MusicEnabled;
import com.github.czyzby.autumn.mvc.stereotype.preference.sfx.MusicVolume;
import com.github.czyzby.autumn.mvc.stereotype.preference.sfx.SoundEnabled;
import com.github.czyzby.autumn.mvc.stereotype.preference.sfx.SoundVolume;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;
import com.github.czyzby.kiwi.util.gdx.asset.lazy.provider.ObjectProvider;
import com.github.czyzby.lml.parser.LmlSyntax;
import com.github.czyzby.lml.util.Lml;
import com.github.czyzby.lml.vis.parser.impl.VisLmlSyntax;
import com.kotcrab.vis.ui.VisUI;

/**
 * Thanks to the Component annotation, this class will be automatically found
 * and processed.
 *
 * This is a utility class that configures application settings.
 */
@Component
public class Configuration {
	
	private static final Logger LOG = LoggerService.forClass(Configuration.class);
	
	/** Name of the application's preferences file. */
	public static final String PREFERENCES = "jCity";
	/** Path to the internationalization bundle. */
	@I18nBundle
	private final String bundlePath = "i18n/bundle";
	/** Enabling VisUI usage. */
	@LmlParserSyntax
	private final LmlSyntax syntax = new VisLmlSyntax();
	/** Parsing macros available in all views. */
	@LmlMacro
	private final String globalMacro = "ui/templates/macros/global.lml";
	
	/**
	 * Using a custom viewport provider - Autumn MVC defaults to the ScreenViewport,
	 * as it is the only viewport that doesn't need to know application's targeted
	 * screen size. This provider overrides that by using more sophisticated
	 * FitViewport that works on virtual units rather than pixels.
	 */
	@StageViewport
	private final ObjectProvider<Viewport> viewportProvider = new ObjectProvider<Viewport>() {
		
		@Override
		public Viewport provide() {
			
			return new FitViewport(CityGame.WIDTH, CityGame.HEIGHT);
		}
	};
	
	/**
	 * These sound-related fields allow MusicService to store settings in
	 * preferences file. Sound preferences will be automatically saved when the
	 * application closes and restored the next time it's turned on. Sound-related
	 * methods methods will be automatically added to LML templates - see
	 * settings.lml template.
	 */
	@SoundVolume(preferences = PREFERENCES)
	private final String soundVolume = "soundVolume";
	@SoundEnabled(preferences = PREFERENCES)
	private final String soundEnabled = "soundOn";
	@MusicVolume(preferences = PREFERENCES)
	private final String musicVolume = "musicVolume";
	@MusicEnabled(preferences = PREFERENCES)
	private final String musicEnabledPreference = "musicOn";
	
	/**
	 * These i18n-related fields will allow LocaleService to save game's locale in
	 * preferences file. Locale changing actions will be automatically added to LML
	 * templates - see settings.lml template.
	 */
	@I18nLocale(propertiesPath = PREFERENCES, defaultLocale = "en")
	private final String localePreference = "locale";
	@AvailableLocales
	private final String[] availableLocales = new String[] { "en" };
	
	/** Setting the default Preferences object path. */
	@Preference
	private final String preferencesPath = PREFERENCES;
	
	/**
	 * Thanks to the Initiate annotation, this method will be automatically invoked
	 * during context building. All method's parameters will be injected with values
	 * from the context.
	 *
	 * @param scaleService
	 *            contains current GUI scale.
	 * @param skinService
	 *            contains GUI skin.
	 */
	@Initiate
	public void initiateConfiguration(final InterfaceService interfaceService, final ScaleService scaleService,
			final SkinService skinService, final AssetService assetService) {
		
		// Loading default VisUI skin with the selected scale:
		VisUI.load(scaleService.getScale());
		
		// Registering VisUI skin with "default" name - this skin will be the default
		// one for all LML widgets:
		skinService.addSkin("default", VisUI.getSkin());
		
		// Thanks to this setting, only methods annotated with @LmlAction will be
		// available in views, significantly
		// speeding up method look-up:
		Lml.EXTRACT_UNANNOTATED_METHODS = false;
		
		addExternalBundles(interfaceService);
		addCustomAssetLoaders(assetService);
	}
	
	private void addExternalBundles(InterfaceService interfaceService) {
		
		LOG.info("Scanning for external bundles ...");
		
		//
		// Does the "data" directory exist in the application root?
		final FileHandle dataRoot = Gdx.files.local("data/");
		if (!dataRoot.exists())
			dataRoot.mkdirs();
		
		final FileHandle bundleRoot = dataRoot.child("bundles/");
		if (!bundleRoot.exists())
			bundleRoot.mkdirs();
			
		//
		// Now: scan for subdirectories.
		for (FileHandle bundleDirectory : bundleRoot.list((f) -> f.isDirectory())) {
			final String bundleName = bundleDirectory.name();
			LOG.info("Loading external bundle [{0}]", bundleName);
			try {
				interfaceService.addBundleFile(bundleName, bundleDirectory.child(bundleName));
			} catch (Throwable t) {
				LOG.error(t,
						"Unable to load external bundle [{0}] -- your bundles are not named the same as their containing directory!",
						bundleDirectory.path());
			}
		}
		
		LOG.info("Finished scanning for external bundles.");
	}
	
	private void addCustomAssetLoaders(AssetService assetService) {
		
		final TileSetLoader tileSetLoader = new TileSetLoader(CityGame.RESOLVER);
		assetService.getAssetManager().setLoader(TileSet.class, "tileset.json", tileSetLoader);
		assetService.getEagerAssetManager().setLoader(TileSet.class, "tileset.json", tileSetLoader);
		
		final JavaMapGeneratorLoader mapGeneratorLoader = new JavaMapGeneratorLoader(CityGame.RESOLVER);
		assetService.getAssetManager().setLoader(MapGeneratorScript.class, ".groovy", mapGeneratorLoader);
		assetService.getEagerAssetManager().setLoader(MapGeneratorScript.class, ".groovy", mapGeneratorLoader);
	}
}