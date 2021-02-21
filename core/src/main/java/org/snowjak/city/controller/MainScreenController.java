/**
 * 
 */
package org.snowjak.city.controller;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewRenderer;
import com.github.czyzby.autumn.mvc.stereotype.View;

/**
 * @author snowjak88
 *
 */
@View(id = "mainScreen", value = "ui/templates/mainScreen.lml")
public class MainScreenController implements ViewRenderer {

	@Override
	public void render(Stage stage, float delta) {
		
		stage.act();
		stage.draw();
	}
	
}
