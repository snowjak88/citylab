package org.snowjak.city;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.snowjak.city.configuration.processors.AssetAnnotationProcessor;
import org.snowjak.city.configuration.processors.InjectAllAnnotationProcessor;
import org.snowjak.city.screens.AbstractGameScreen;
import org.snowjak.city.screens.LoadingScreen;
import org.snowjak.city.screens.MainMenuScreen;
import org.snowjak.city.screens.loadingtasks.AssetServiceLoadingTask;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.github.czyzby.autumn.context.ContextDestroyer;
import com.github.czyzby.autumn.context.ContextInitializer;
import com.github.czyzby.autumn.scanner.ClassScanner;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

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
	 * Directory holding internal skin-definitions.
	 */
	public static final String INTERNAL_SKIN_BASE = "ui/skins/";
	
	/**
	 * Directory holding external I18N bundles
	 */
	public static final String EXTERNAL_ROOT_BUNDLES = "data/bundles/";
	
	/**
	 * Directory holding map-generator scripts
	 */
	public static final String EXTERNAL_ROOT_MAP_GENERATORS = "data/map-generators/";
	
	//
	//
	//
	
	/**
	 * Thread-caching {@link ExecutorService}.
	 */
	public static final ListeningExecutorService EXECUTOR = MoreExecutors.listeningDecorator(MoreExecutors
			.getExitingExecutorService((ThreadPoolExecutor) Executors.newCachedThreadPool(), Duration.ofSeconds(5)));
	
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
		
		//
		// Registering custom annotation processors.
		initializer.addProcessor(new InjectAllAnnotationProcessor());
		
		//
		// AssetAnnotationProcessor is registered as a *component*, not a *processor*,
		// because it requires one or more components to be injected into it.
		initializer.addComponent(new AssetAnnotationProcessor());
		
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
		// Set the very first Screen instance -- the loading screen, currently
		// configured to wait until the GameAssetService has finished, and then switch
		// to the Main-Menu screen.
		//
		loadingScreen.setLoadingTask(assetServiceLoadingTask);
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