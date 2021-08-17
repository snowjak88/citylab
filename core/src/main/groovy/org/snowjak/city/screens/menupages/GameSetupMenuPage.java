/**
 * 
 */
package org.snowjak.city.screens.menupages;

import java.util.stream.Collectors;

import org.snowjak.city.GameData;
import org.snowjak.city.configuration.InitPriority;
import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.screens.MainMenuScreen;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.MapGeneratorService;
import org.snowjak.city.service.SkinService;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.kotcrab.vis.ui.building.StandardTableBuilder;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

/**
 * @author snowjak88
 *
 */
@Component
public class GameSetupMenuPage implements MainMenuPage {
	
	@Inject
	private SkinService skinService;
	
	@Inject
	private I18NService i18nService;
	
	@Inject
	private MapGeneratorService mapGeneratorService;
	
	private Table root;
	private Spinner mapWidthSpinner, mapHeightSpinner;
	private VisSelectBox<MapGenerator> mapGeneratorSelection;
	private VisTextButton startGameButton;
	
	private Runnable onGameStart;
	
	@Initiate(priority = InitPriority.LOW_PRIORITY)
	public void init() {
		
		final Skin defaultSkin = skinService.getSkin("default");
		
		final GameData.GameParameters param = GameData.get().parameters;
		
		mapWidthSpinner = new Spinner(i18nService.get("menu-gamesetup-map-width"),
				new IntSpinnerModel(64, 64, 1024, 64));
		mapWidthSpinner.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				final IntSpinnerModel model = (IntSpinnerModel) ((Spinner) actor).getModel();
				param.mapWidth = model.getValue();
				
				checkStartGameButton();
			}
		});
		
		mapHeightSpinner = new Spinner(i18nService.get("menu-gamesetup-map-height"),
				new IntSpinnerModel(64, 64, 1024, 64));
		mapHeightSpinner.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				final IntSpinnerModel model = (IntSpinnerModel) ((Spinner) actor).getModel();
				param.mapHeight = model.getValue();
				
				checkStartGameButton();
			}
		});
		
		mapGeneratorSelection = new VisSelectBox<MapGenerator>() {
			
			@Override
			protected String toString(MapGenerator item) {
				
				return item.getTitle();
			}
			
		};
		mapGeneratorSelection.addListener(new ChangeListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				param.selectedMapGenerator = ((VisSelectBox<MapGenerator>) actor).getSelected();
				param.selectedMapGeneratorName = null;
				
				checkStartGameButton();
			}
		});
		
		startGameButton = new VisTextButton(i18nService.get("menu-gamesetup-start"), new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				if (onGameStart != null)
					onGameStart.run();
			}
		});
		
		//@formatter:off
		root = new StandardTableBuilder()
				.append(mapWidthSpinner)
			.row()
				.append(mapHeightSpinner)
			.row()
				.append(new Label(i18nService.get("menu-gamesetup-map-generator"), defaultSkin))
				.append(mapGeneratorSelection)
			.row()
				.append(startGameButton)
			.build();
		//@formatter:on
		
		checkStartGameButton();
	}
	
	public void setOnGameStart(Runnable onGameStart) {
		
		this.onGameStart = onGameStart;
	}
	
	private void checkStartGameButton() {
		
		boolean isValid = true;
		
		if (((IntSpinnerModel) mapWidthSpinner.getModel()).getValue() <= 0)
			isValid = false;
		if (((IntSpinnerModel) mapHeightSpinner.getModel()).getValue() <= 0)
			isValid = false;
		if (mapGeneratorSelection.getSelected() == null)
			isValid = false;
		
		startGameButton.setDisabled(!isValid);
	}
	
	@Override
	public void show() {
		
		final Array<MapGenerator> availableMapGenerators = new Array<>(mapGeneratorService.getLoadedNames().stream()
				.map(n -> mapGeneratorService.get(n, true)).collect(Collectors.toList()).toArray(new MapGenerator[0]));
		mapGeneratorSelection.setItems(availableMapGenerators);
	}
	
	@Override
	public void hide() {
		
	}
	
	@Override
	public Actor getRoot(MainMenuScreen mainMenuScreen) {
		
		return root;
	}
	
}
