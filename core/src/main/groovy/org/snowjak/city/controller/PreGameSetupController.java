/**
 * 
 */
package org.snowjak.city.controller;

import java.util.ArrayList;
import java.util.List;

import org.snowjak.city.GameData;
import org.snowjak.city.service.MapGeneratorService;
import org.snowjak.city.service.TileSetService;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.github.czyzby.autumn.annotation.Inject;
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
public class PreGameSetupController implements ViewRenderer, ActionContainer {
	
	@Inject
	private MapGeneratorService mapGeneratorService;
	
	@Inject
	private TileSetService tilesetService;
	
	@LmlActor("playButton")
	private TextButton playButton;
	
	private GameData gameData = GameData.get();
	
	@LmlAction
	public List<String> getAvailableTilesets() {
		
		return new ArrayList<>(tilesetService.getScriptNames());
	}
	
	@LmlAction
	public void setTileset(final Actor actor) {
		
		@SuppressWarnings("unchecked")
		final SelectBox<String> selectBox = (SelectBox<String>) actor;
		gameData.parameters.selectedTileset = null;
		gameData.parameters.selectedTilesetName = selectBox.getSelected();
	}
	
	@LmlAction
	public List<String> getAvailableGenerators() {
		
		return new ArrayList<>(mapGeneratorService.getScriptNames());
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
		final SelectBox<String> selectBox = (SelectBox<String>) actor;
		gameData.parameters.selectedMapGenerator = null;
		gameData.parameters.selectedMapGeneratorName = selectBox.getSelected();
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
}
