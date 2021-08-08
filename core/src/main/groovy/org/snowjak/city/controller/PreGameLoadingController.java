/**
 * 
 */
package org.snowjak.city.controller;

import java.util.concurrent.atomic.AtomicReference;

import org.snowjak.city.GameData;
import org.snowjak.city.GameData.GameParameters;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.service.MapGeneratorService;
import org.snowjak.city.service.TileSetService;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewController;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewInitializer;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewRenderer;
import com.github.czyzby.autumn.mvc.stereotype.View;
import com.github.czyzby.lml.annotation.LmlActor;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * @author snowjak88
 *
 */
@View(id = "preGameLoading", value = "ui/templates/preGameLoading.lml")
public class PreGameLoadingController implements ViewInitializer, ViewRenderer {
	
	@Inject
	private MapGeneratorService mapGeneratorService;
	
	@Inject
	private TileSetService tilesetService;
	
	@Inject
	private InterfaceService interfaceService;
	
	@LmlActor("loadingStatus")
	private Label loadingStatus;
	
	@LmlActor("loadingBar")
	private ProgressBar progressBar;
	
	private final AtomicDouble loadingProgress = new AtomicDouble(0);
	private final AtomicReference<GameSetupStage> loadingStage = new AtomicReference<>(GameSetupStage.NOT_STARTED);
	
	private ListenableFuture<?> setupThread;
	
	@Override
	public void initialize(Stage stage, ObjectMap<String, Actor> actorMappedByIds) {
		
		setupThread = GameData.get().executor
				.submit(new NewGameSetupThread(tilesetService, mapGeneratorService, loadingProgress, loadingStage));
	}
	
	@Override
	public void render(Stage stage, float delta) {
		
		final GameSetupStage setupStage = loadingStage.get();
		final String stageText;
		if (setupStage == null)
			stageText = "";
		else
			stageText = setupStage.getBundleLine();
		
		loadingStatus.setText(interfaceService.getParser().parseString(stageText));
		progressBar.setValue((float) loadingProgress.get());
		
		stage.act(delta);
		stage.draw();
		
		//
		// If setup is done -- switch to the game-screen
		//
		if (setupStage == GameSetupStage.DONE && setupThread.isDone())
			interfaceService.show(GameScreenController.class);
	}
	
	@Override
	public void destroy(ViewController viewController) {
		
		//
		// nothing to do
	}
	
	public enum GameSetupStage {
		
		NOT_STARTED("@pregame-status-notstarted"),
		GENERATE_MAP("@pregame-status-generatemap"),
		APPLY_TILESET("@pregame-status-applytileset"),
		DONE("@pregame-status-done");
		
		private final String bundleLine;
		
		GameSetupStage(String bundleLine) {
			
			this.bundleLine = bundleLine;
		}
		
		public String getBundleLine() {
			
			return bundleLine;
		}
	}
	
	/**
	 * Sets up a new game (i.e. configures {@link GameData}) using parameters given
	 * in {@link GameData#parameters}.
	 * 
	 * @author snowjak88
	 *
	 */
	public class NewGameSetupThread implements Runnable {
		
		private final TileSetService tileSetService;
		private final MapGeneratorService mapGeneratorService;
		private final AtomicDouble loadingProgress;
		private final AtomicReference<GameSetupStage> loadingStage;
		
		NewGameSetupThread(TileSetService tileSetService, MapGeneratorService mapGeneratorService,
				AtomicDouble loadingProgress, AtomicReference<GameSetupStage> loadingStage) {
			
			this.tileSetService = tileSetService;
			this.mapGeneratorService = mapGeneratorService;
			this.loadingProgress = loadingProgress;
			this.loadingStage = loadingStage;
		}
		
		@Override
		public void run() {
			
			try {
				loadingProgress.set(0);
				
				final GameData data = GameData.get();
				if (data.parameters == null)
					data.parameters = new GameParameters();
				
				final GameParameters param = data.parameters;
				
				data.tileset = (param.selectedTileset != null) ? param.selectedTileset
						: tileSetService.getTileSet(param.selectedTilesetName);
				
				if (param.seed != null && !param.seed.isEmpty())
					data.seed = param.seed;
				
				final MapGenerator generator = (param.selectedMapGenerator != null) ? param.selectedMapGenerator
						: mapGeneratorService.getGenerator(param.selectedMapGeneratorName);
				
				generator.setSeed(data.seed);
				
				loadingStage.set(GameSetupStage.GENERATE_MAP);
				data.map = generator.generate(param.mapWidth, param.mapHeight, (p) -> loadingProgress.set(p / 2d));
				
				loadingStage.set(GameSetupStage.APPLY_TILESET);
				data.map.updateTiles((p) -> loadingProgress.set(p / 2d + 0.5d));
				
				loadingProgress.set(1.0);
				loadingStage.set(GameSetupStage.DONE);
				
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}
