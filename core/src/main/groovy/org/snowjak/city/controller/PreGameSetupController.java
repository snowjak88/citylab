/**
 * 
 */
package org.snowjak.city.controller;

import java.util.ArrayList;
import java.util.List;

import org.snowjak.city.GameData;
import org.snowjak.city.service.MapGeneratorService;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.stereotype.View;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.github.czyzby.lml.util.LmlUtilities;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

/**
 * @author snowjak88
 *
 */
@View(id = "preGameSetup", value = "ui/templates/preGameSetup.lml")
public class PreGameSetupController implements ActionContainer {
	
	@Inject
	private MapGeneratorService mapGeneratorService;
	
	@LmlActor("playButton")
	private TextButton playButton;
	
	private GameData gameData = GameData.get();
	
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
		
		checkPlayButton();
	}
	
	@LmlAction
	public void setMapGenerator(final Actor actor) {
		
		@SuppressWarnings("unchecked")
		final SelectBox<String> selectBox = (SelectBox<String>) actor;
		gameData.parameters.selectedMapGenerator = null;
		gameData.parameters.selectedMapGeneratorName = selectBox.getSelected();
		
		checkPlayButton();
	}
	
	@LmlAction
	public int getMapWidth() {
		
		return gameData.parameters.mapWidth;
	}
	
	@LmlAction
	public int getMapHeight() {
		
		return gameData.parameters.mapHeight;
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
