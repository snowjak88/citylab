/**
 * 
 */
package org.snowjak.city.controller;

import org.snowjak.city.screen.Screen;
import org.snowjak.city.screen.impl.GreenScreen;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewRenderer;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewResizer;
import com.github.czyzby.autumn.mvc.stereotype.View;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.action.ActionContainer;

/**
 * @author snowjak88
 *
 */
@View(id = "mainScreen", value = "ui/templates/mainScreen.lml")
public class MainScreenController implements ViewRenderer, ViewResizer, ActionContainer {
	
	@Inject
	private InterfaceService interfaceService;
	
	@LmlActor("mainScreenContainer")
	private Container<Actor> mainScreenContainer;
	
	private Screen activeScreen = new GreenScreen();
	
	public MainScreenController() {
		
		setScreen(new GreenScreen());
		;
	}
	
	@Override
	public void render(Stage stage, float delta) {
		
		stage.act();
		
		if (activeScreen != null) {
			
			activeScreen.getViewport().apply(true);
			
			activeScreen.render(delta);
			
			stage.getViewport().apply(true);
		}
		
		stage.draw();
	}
	
	@Override
	public void resize(Stage stage, int width, int height) {
		
		if (activeScreen != null)
			activeScreen.getViewport().update(width, height, true);
	}
	
	/**
	 * Sets up the given screen as the active {@link Screen}. Handles
	 * {@link InputProcessor} registration and all that.
	 * 
	 * @param screen
	 */
	public void setScreen(Screen screen) {
		
		this.activeScreen = screen;
		
		if (activeScreen != null)
			this.activeScreen.setScreenTransitionHandler((n) -> setScreen(n));
	}
	
}
