package org.snowjak.city;

import org.apache.groovy.util.Maps;
import org.snowjak.city.configuration.MatchingFileHandleResolver;
import org.snowjak.city.screens.AbstractGameScreen;
import org.snowjak.city.screens.LoadingScreen;
import org.snowjak.city.screens.MainMenuScreen;
import org.snowjak.city.screens.loadingtasks.AssetServiceLoadingTask;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.czyzby.autumn.context.ContextDestroyer;
import com.github.czyzby.autumn.context.ContextInitializer;
import com.github.czyzby.autumn.scanner.ClassScanner;

/**
 * Entry point for this application. Configures the Autumn context for
 * dependency-injection.
 */
public class CityGame extends Game {
	
	/** Default application size. */
	public static final int WIDTH = 800, HEIGHT = 600;
	
	/**
	 * The internal I18N bundle has this base-name
	 */
	public static final String INTERNAL_BUNDLE_BASE = "i18n/bundle";
	
	/**
	 * Directory holding external I18N bundles
	 */
	public static final String EXTERNAL_ROOT_BUNDLES = "data/bundles/";
	
	/**
	 * Directory holding map-generator scripts
	 */
	public static final String EXTERNAL_ROOT_MAP_GENERATORS = "data/map-generators/";
	
	/**
	 * Directory holding module-definition scripts
	 */
	public static final String EXTERNAL_ROOT_MODULES = "data/modules/";
	
	/**
	 * Directory holding tileset-definition scripts
	 */
	public static final String EXTERNAL_ROOT_TILESETS = "data/tilesets/";
	
	/**
	 * Application-specific {@link FileHandleResolver}, configured to handle
	 * internal- and external-resources equally well.
	 */
	public static final FileHandleResolver RESOLVER = new MatchingFileHandleResolver(
			Maps.of("^/?data/.*", new LocalFileHandleResolver()), new InternalFileHandleResolver());
	
	//
	//
	//
	
	private LoadingScreen loadingScreen;
	private MainMenuScreen mainMenuScreen;
	private AssetServiceLoadingTask assetServiceLoadingTask;
	
	private final ClassScanner scanner;
	
	private ContextDestroyer destroyer;
	
	public CityGame(ClassScanner scanner) {
		
		super();
		
		this.scanner = scanner;
	}
	
	@Override
	public void create() {
		
		// Preparing Autumn context:
		final ContextInitializer initializer = new ContextInitializer();
		
		// Registering platform-specific scanner. Starting to scan classes from Root:
		initializer.scan(CityGame.class, scanner);
		
		initializer.doAfterInitiation((ctx) -> {
			this.loadingScreen = (LoadingScreen) ctx.getComponent(LoadingScreen.class);
			this.mainMenuScreen = (MainMenuScreen) ctx.getComponent(MainMenuScreen.class);
			this.assetServiceLoadingTask = (AssetServiceLoadingTask) ctx.getComponent(AssetServiceLoadingTask.class);
		});
		
		//
		// Creating context:
		destroyer = initializer.initiate();
		
		//
		// Set the very first Screen instance -- the loading screen!
		loadingScreen.setLoadingTasks(assetServiceLoadingTask);
		loadingScreen.setLoadingCompleteAction(() -> loadingScreen.changeScreen(mainMenuScreen));
		setScreen(loadingScreen);
	}
	
	@Override
	public void setScreen(Screen screen) {
		
		super.setScreen(screen);
		
		if (screen instanceof AbstractGameScreen)
			((AbstractGameScreen) screen).setGame(this);
	}
	
	@Override
	public void dispose() {
		
		super.dispose();
		
		destroyer.dispose();
	}
}