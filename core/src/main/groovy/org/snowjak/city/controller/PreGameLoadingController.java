/**
 * 
 */
package org.snowjak.city.controller;

import java.util.concurrent.atomic.AtomicReference;

import org.snowjak.city.GameData;
import org.snowjak.city.GameData.GameParameters;
import org.snowjak.city.ecs.components.UpdatedMapCell;
import org.snowjak.city.ecs.systems.MapCellUpdatingSystem;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.module.Module;
import org.snowjak.city.service.MapGeneratorService;
import org.snowjak.city.service.ModuleService;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
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
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;
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
	private ModuleService moduleService;
	
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
				.submit(new NewGameSetupThread(moduleService, mapGeneratorService, loadingProgress, loadingStage));
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
		
		private final Logger LOG = LoggerService.forClass(NewGameSetupThread.class);
		
		private final ModuleService moduleService;
		private final MapGeneratorService mapGeneratorService;
		private final AtomicDouble loadingProgress;
		private final AtomicReference<GameSetupStage> loadingStage;
		
		NewGameSetupThread(ModuleService moduleService, MapGeneratorService mapGeneratorService,
				AtomicDouble loadingProgress, AtomicReference<GameSetupStage> loadingStage) {
			
			this.moduleService = moduleService;
			this.mapGeneratorService = mapGeneratorService;
			this.loadingProgress = loadingProgress;
			this.loadingStage = loadingStage;
		}
		
		@Override
		public void run() {
			
			try {
				LOG.info("Starting pre-game setup ...");
				
				loadingProgress.set(0);
				
				final GameData data = GameData.get();
				if (data.parameters == null)
					data.parameters = new GameParameters();
				
				final GameParameters param = data.parameters;
				
				if (param.seed != null && !param.seed.isEmpty())
					data.seed = param.seed;
				LOG.info("World-seed = \"{0}\"", data.seed);
				
				final MapGenerator generator = (param.selectedMapGenerator != null) ? param.selectedMapGenerator
						: mapGeneratorService.get(param.selectedMapGeneratorName);
				
				generator.setSeed(data.seed);
				
				LOG.info("Generating the map ...");
				loadingStage.set(GameSetupStage.GENERATE_MAP);
				data.map = generator.generate(param.mapWidth, param.mapHeight, (p) -> loadingProgress.set(p / 2d));
				
				LOG.info("Setting up the entity-processing engine ...");
				if (data.entityEngine == null)
					data.entityEngine = new Engine();
					
				//
				// Add default systems ...
				data.entityEngine.addSystem(new MapCellUpdatingSystem());
				
				//
				// Add Entities for every map-cell ...
				for (int x = 0; x < data.map.getWidth(); x++)
					for (int y = 0; y < data.map.getHeight(); y++) {
						final Entity entity = data.entityEngine.createEntity();
						final UpdatedMapCell mapCell = (UpdatedMapCell) entity.addAndReturn(new UpdatedMapCell());
						mapCell.setCellX(x);
						mapCell.setCellY(y);
						data.entityEngine.addEntity(entity);
					}
				
				LOG.info("Initializing modules ...");
				for (String moduleName : moduleService.getLoadedNames()) {
					
					final Module module = moduleService.get(moduleName);
					
					//
					// Register rendering hooks with the main GameData instance
					//
					
					if (!module.getCellRenderingHooks().isEmpty()) {
						LOG.info("Module [{0}]: registering map-rendering-hooks ...", moduleName);
						data.cellRenderingHooks.addAll(module.getCellRenderingHooks());
					}
					if (!module.getCustomRenderingHooks().isEmpty()) {
						LOG.info("Module [{0}]: registering rendering-hooks ...", moduleName);
						data.customRenderingHooks.addAll(module.getCustomRenderingHooks());
					}
					
					//
					// Add this module's systems to the entity engine
					module.getSystems().entrySet().forEach(e -> {
						LOG.info("Module [{0}]: adding entity-processing system [{1}] ...", moduleName, e.getKey());
						data.entityEngine.addSystem(e.getValue());
					});
				}
				LOG.info("Finished initializing modules.");
				
				loadingProgress.set(1.0);
				loadingStage.set(GameSetupStage.DONE);
				
				LOG.info("Finished pre-game setup.");
				
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}
