/**
 * 
 */
package org.snowjak.city.controller;

import java.util.stream.Collectors;

import org.snowjak.city.GameData;
import org.snowjak.city.service.MapGeneratorService;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewController;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewInitializer;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewRenderer;
import com.github.czyzby.autumn.mvc.stereotype.View;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.github.czyzby.lml.util.LmlUtilities;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

/**
 * @author snowjak88
 *
 */
@View(id = "preGameSetup", value = "ui/templates/preGameSetup.lml")
public class PreGameSetupController implements ViewInitializer, ViewRenderer, ActionContainer {
	
	@Inject
	private MapGeneratorService mapGeneratorService;
	
	@LmlActor("map-generator-select")
	private SelectBox<MapGeneratorBean> mapGeneratorSelect;
	
	@LmlActor("playButton")
	private TextButton playButton;
	
	private GameData gameData = GameData.get();
	
	@Override
	public void initialize(Stage stage, ObjectMap<String, Actor> actorMappedByIds) {
		
		mapGeneratorSelect.setItems(getAvailableGenerators());
	}
	
	public Array<MapGeneratorBean> getAvailableGenerators() {
		
		return new Array<>(mapGeneratorService.getLoadedNames().stream()
				.map(n -> new MapGeneratorBean(n, mapGeneratorService.get(n).getTitle())).collect(Collectors.toList())
				.toArray(new MapGeneratorBean[0]));
	}
	
	@LmlAction
	public void setMapDimension(final Actor actor) {
		
		final Spinner spinner = (Spinner) actor;
		final IntSpinnerModel model = (IntSpinnerModel) spinner.getModel();
		
		if (LmlUtilities.getActorId(actor).equalsIgnoreCase("mapWidth"))
			gameData.parameters.mapWidth = model.getValue();
		else if (LmlUtilities.getActorId(actor).equalsIgnoreCase("mapHeight"))
			gameData.parameters.mapHeight = model.getValue();
	}
	
	@LmlAction
	public void setMapGenerator(final Actor actor) {
		
		@SuppressWarnings("unchecked")
		final SelectBox<MapGeneratorBean> selectBox = (SelectBox<MapGeneratorBean>) actor;
		gameData.parameters.selectedMapGenerator = null;
		gameData.parameters.selectedMapGeneratorName = selectBox.getSelected().name;
	}
	
	@LmlAction
	public int getMapWidth() {
		
		return gameData.parameters.mapWidth;
	}
	
	@LmlAction
	public int getMapHeight() {
		
		return gameData.parameters.mapHeight;
	}
	
	@LmlAction
	public String getSeed() {
		
		return gameData.parameters.seed;
	}
	
	@LmlAction
	public void setSeed(Actor seedField) {
		
		gameData.parameters.seed = ((VisTextField) seedField).getText();
	}
	
	@Override
	public void render(Stage stage, float delta) {
		
		checkPlayButton();
		
		stage.act(delta);
		stage.draw();
	}
	
	@Override
	public void destroy(ViewController viewController) {
		
		//
		// nothing to do
	}
	
	private void checkPlayButton() {
		
		if (playButton == null)
			return;
		
		boolean configValid = true;
		
		if (gameData.parameters.mapHeight <= 0)
			configValid = false;
		if (gameData.parameters.mapWidth <= 0)
			configValid = false;
		if (gameData.parameters.selectedMapGenerator == null && (gameData.parameters.selectedMapGeneratorName == null
				|| gameData.parameters.selectedMapGeneratorName.isEmpty()))
			configValid = false;
		
		playButton.setDisabled(!configValid);
	}
	
	public static class MapGeneratorBean {
		
		public String name, title;
		
		public MapGeneratorBean() {
			
		}
		
		public MapGeneratorBean(String name, String title) {
			
			this.name = name;
			this.title = title;
		}
		
		@Override
		public String toString() {
			
			return title;
		}
	}
}
