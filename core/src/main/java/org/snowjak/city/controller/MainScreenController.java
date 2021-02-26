/**
 * 
 */
package org.snowjak.city.controller;

import org.snowjak.city.screen.AbstractScreen;
import org.snowjak.city.service.ScreenService;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewController;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewInitializer;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewRenderer;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewResizer;
import com.github.czyzby.autumn.mvc.stereotype.View;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.github.czyzby.lml.util.LmlUtilities;

/**
 * @author snowjak88
 *
 */
@View(id = "mainScreen", value = "ui/templates/mainScreen.lml")
public class MainScreenController implements ViewInitializer, ViewRenderer, ViewResizer, ActionContainer {
	
	@Inject
	private ScreenService screenService;
	
	private AbstractScreen activeScreen;
	
	@ViewStage
	private Stage stage;
	
	@Override
	public void initialize(Stage stage, ObjectMap<String, Actor> actorMappedByIds) {
		
		addInputListener();
	}
	
	@Override
	public void render(Stage stage, float delta) {
		
		stage.act();
		
		if (activeScreen != null) {
			
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
	
	@LmlAction
	public void setScreen(Actor button) {
		
		setScreen(screenService.getScreen(LmlUtilities.getActorId(button)));
	}
	
	/**
	 * Sets up the given screen as the active {@link AbstractScreen}. Handles
	 * {@link InputProcessor} registration and all that.
	 * 
	 * @param screen
	 */
	public void setScreen(AbstractScreen screen) {
		
		if (activeScreen != null)
			activeScreen.removed();
		
		removeInputListener();
		
		this.activeScreen = screen;
		
		if (activeScreen != null)
			activeScreen.setScreenTransitionHandler((n) -> setScreen(n));
		
		addInputListener();
		resize(stage, (int) stage.getWidth(), (int) stage.getHeight());
		
		if (activeScreen != null)
			activeScreen.added();
	}
	
	@LmlAction
	public String[] getScreens() {
		
		return screenService.getScreenNames().toArray(new String[0]);
	}
	
	private void removeInputListener() {
		
		if (stage != null && activeScreen != null)
			stage.removeListener(activeScreen);
	}
	
	private void addInputListener() {
		
		if (stage != null && activeScreen != null)
			stage.addListener(activeScreen);
	}
	
	@Override
	public void destroy(ViewController viewController) {
		
		removeInputListener();
	}
}
