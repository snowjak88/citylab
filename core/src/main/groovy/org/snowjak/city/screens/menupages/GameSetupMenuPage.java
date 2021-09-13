/**
 * 
 */
package org.snowjak.city.screens.menupages;

import java.util.stream.Collectors;

import org.snowjak.city.map.generator.MapGenerator;
import org.snowjak.city.module.Module;
import org.snowjak.city.module.ui.VisualParameter;
import org.snowjak.city.screens.MainMenuScreen;
import org.snowjak.city.service.GameAssetService;
import org.snowjak.city.service.GameService.NewGameParameters;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.MapGeneratorService;
import org.snowjak.city.service.SkinService;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;

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
	
	@Inject
	private GameAssetService assetService;
	
	private ScrollPane screenScroll;
	private Table formTable;
	
	private TextField mapWidthField, mapHeightField;
	private Button mapWidthIncrease, mapWidthDecrease, mapHeightIncrease, mapHeightDecrease;
	
	private SelectBox<MapGenerator> mapGeneratorSelection;
	
	private TextField seedField;
	
	private TextButton startGameButton;
	
	private Runnable onGameStart;
	
	final NewGameParameters param = new NewGameParameters();
	
	public void setOnGameStart(Runnable onGameStart) {
		
		this.onGameStart = onGameStart;
	}
	
	@Override
	public void show() {
		
		final Array<MapGenerator> availableMapGenerators = new Array<>(mapGeneratorService.getLoadedNames().stream()
				.map(n -> mapGeneratorService.get(n, true)).collect(Collectors.toList()).toArray(new MapGenerator[0]));
		
		mapGeneratorSelection.setItems(availableMapGenerators);
		
		if (!availableMapGenerators.isEmpty()) {
			param.setGenerator(availableMapGenerators.first());
			mapGeneratorSelection.setSelectedIndex(0);
		}
	}
	
	@Override
	public void hide() {
		
	}
	
	@Override
	public Actor getRoot(MainMenuScreen mainMenuScreen) {
		
		if (screenScroll == null)
			constructScreen();
		
		return screenScroll;
	}
	
	private void constructScreen() {
		
		final Skin skin = skinService.getCurrent();
		
		mapWidthField = new TextField(Integer.toString(param.getMapWidth()), skin);
		mapWidthIncrease = new Button(skin, "plus");
		mapWidthDecrease = new Button(skin, "minus");
		
		mapWidthField.setProgrammaticChangeEvents(true);
		mapWidthField.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				final String newText = ((TextField) event.getTarget()).getText();
				if (newText.isEmpty())
					return;
				try {
					final int newValue = Integer.parseInt(newText);
					
					param.setMapWidth(newValue);
				} catch (NumberFormatException e) {
					event.cancel();
				}
			}
		});
		
		mapWidthIncrease.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				final String width = mapWidthField.getText();
				if (width.isEmpty()) {
					mapWidthField.setText("64");
					param.setMapWidth(64);
					return;
				}
				int widthValue = Integer.parseInt(width);
				if (widthValue >= 960)
					widthValue = 960;
				
				mapWidthField.setText(Integer.toString(widthValue + 64));
			}
		});
		
		mapWidthDecrease.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				final String width = mapWidthField.getText();
				if (width.isEmpty()) {
					mapWidthField.setText("64");
					param.setMapWidth(64);
					return;
				}
				int widthValue = Integer.parseInt(width);
				if (widthValue <= 128)
					widthValue = 128;
				
				mapWidthField.setText(Integer.toString(widthValue - 64));
			}
		});
		
		mapHeightField = new TextField(Integer.toString(param.getMapHeight()), skin);
		mapHeightIncrease = new Button(skin, "plus");
		mapHeightDecrease = new Button(skin, "minus");
		
		mapHeightField.setProgrammaticChangeEvents(true);
		mapHeightField.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				final String newText = ((TextField) event.getTarget()).getText();
				if (newText.isEmpty())
					return;
				try {
					final int newValue = Integer.parseInt(newText);
					
					param.setMapHeight(newValue);
				} catch (NumberFormatException e) {
					event.cancel();
				}
			}
		});
		
		mapHeightIncrease.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				final String width = mapHeightField.getText();
				if (width.isEmpty()) {
					mapHeightField.setText("64");
					param.setMapHeight(64);
					return;
				}
				int widthValue = Integer.parseInt(width);
				if (widthValue >= 960)
					widthValue = 960;
				
				mapHeightField.setText(Integer.toString(widthValue + 64));
			}
		});
		
		mapHeightDecrease.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				final String width = mapHeightField.getText();
				if (width.isEmpty()) {
					mapHeightField.setText("64");
					param.setMapHeight(64);
					return;
				}
				int widthValue = Integer.parseInt(width);
				if (widthValue <= 128)
					widthValue = 128;
				
				mapHeightField.setText(Integer.toString(widthValue - 64));
			}
		});
		
		mapGeneratorSelection = new SelectBox<MapGenerator>(skin) {
			
			@Override
			protected String toString(MapGenerator item) {
				
				return item.getTitle();
			}
			
		};
		mapGeneratorSelection.addListener(new ChangeListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				param.setGenerator(((SelectBox<MapGenerator>) actor).getSelected());
				
				checkStartGameButton();
			}
		});
		
		seedField = new TextField("", skin);
		seedField.setText(param.getSeed());
		seedField.setMessageText(i18nService.get("menu-gamesetup-seed-blank"));
		seedField.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				final TextField tf = (TextField) actor;
				param.setSeed(tf.getText());
			}
		});
		
		startGameButton = new TextButton(i18nService.get("menu-gamesetup-start"), skin);
		startGameButton.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				if (onGameStart != null)
					onGameStart.run();
			}
		});
		
		final HorizontalGroup mapWidthButtons = new HorizontalGroup();
		mapWidthButtons.addActor(mapWidthDecrease);
		mapWidthButtons.addActor(mapWidthIncrease);
		
		final HorizontalGroup mapHeightButtons = new HorizontalGroup();
		mapHeightButtons.addActor(mapHeightDecrease);
		mapHeightButtons.addActor(mapHeightIncrease);
		
		formTable = new Table(skin);
		formTable.row().spaceBottom(15).spaceRight(5);
		formTable.add(i18nService.get("menu-gamesetup-map-width")).right().expandX();
		formTable.add(mapWidthField);
		formTable.add(mapWidthButtons).left();
		
		formTable.row().spaceBottom(15).spaceRight(5);
		formTable.add(i18nService.get("menu-gamesetup-map-height")).right().expandX();
		formTable.add(mapHeightField, mapHeightButtons);
		
		formTable.row().spaceBottom(15).spaceRight(5);
		formTable.add(i18nService.get("menu-gamesetup-map-generator"));
		formTable.add(mapGeneratorSelection).colspan(2).fillX();
		
		formTable.row().spaceBottom(15).spaceRight(5);
		formTable.add(i18nService.get("menu-gamesetup-seed"));
		formTable.add(seedField).colspan(2).fillX();
		
		addVisualParameters(skin);
		
		formTable.row().spaceBottom(15).spaceRight(5);
		formTable.add(startGameButton).colspan(3).right();
		
		screenScroll = new ScrollPane(formTable);
		screenScroll.setFadeScrollBars(true);
		screenScroll.setScrollingDisabled(true, false);
		
		checkStartGameButton();
	}
	
	private void addVisualParameters(Skin skin) {
		
		for (Module m : assetService.getAllByType(Module.class)) {
			
			if (m.getVisualParameters().isEmpty())
				continue;
			
			formTable.row().spaceBottom(15).spaceRight(5);
			formTable.add(m.getTitle()).colspan(3).center();
			
			for (VisualParameter p : m.getVisualParameters()) {
				formTable.row().spaceBottom(15).spaceRight(5);
				formTable.add(p.getTitle()).right();
				formTable.add(p.getType().getActor(skin)).colspan(2);
			}
		}
	}
	
	private void checkStartGameButton() {
		
		boolean isValid = true;
		
		int mapWidth = 0, mapHeight = 0;
		try {
			mapWidth = Integer.parseInt(mapWidthField.getText());
			mapHeight = Integer.parseInt(mapHeightField.getText());
		} catch (NumberFormatException e) {
			isValid = false;
		}
		
		if ((mapWidth <= 0) || (mapHeight <= 0))
			isValid = false;
		if (mapGeneratorSelection.getSelected() == null)
			isValid = false;
		
		startGameButton.setDisabled(!isValid);
	}
	
	/**
	 * @return the {@link NewGameParameters} that this setup-screen will be
	 *         configuring
	 */
	public NewGameParameters getNewGameParameters() {
		
		return param;
	}
}
