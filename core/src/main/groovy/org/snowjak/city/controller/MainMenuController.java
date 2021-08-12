package org.snowjak.city.controller;

import org.snowjak.city.controller.dialog.ResourceLoadFailuresDialogController;
import org.snowjak.city.service.GameAssetService;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewController;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewInitializer;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewRenderer;
import com.github.czyzby.autumn.mvc.stereotype.View;

/**
 * Presents the main menu.
 * 
 * @author snowjak88
 *
 */
@View(id = "menu", value = "ui/templates/menu.lml", themes = "music/theme.ogg")
public class MainMenuController implements ViewInitializer, ViewRenderer {
	
	private Texture logo;
	
	@Inject
	GameAssetService assetService;
	
	@Inject
	private InterfaceService interfaceService;
	
	@Override
	public void initialize(Stage stage, ObjectMap<String, Actor> actorMappedByIds) {
		
		final String logoFileName = "images/libgdx.png";
		assetService.load(logoFileName, Texture.class);
		assetService.finishLoading(logoFileName);
		logo = assetService.get(logoFileName, Texture.class);
		
		if (!(assetService.getLoadFailures().isEmpty()))
			interfaceService.showDialog(ResourceLoadFailuresDialogController.class);
	}
	
	@Override
	public void render(final Stage stage, final float delta) {
		
		stage.act(delta);
		
		final Batch batch = stage.getBatch();
		batch.setColor(stage.getRoot().getColor()); // We want the logo to share color alpha with the stage.
		batch.begin();
		batch.draw(logo, (int) (stage.getWidth() - logo.getWidth()) / 2,
				(int) (stage.getHeight() - logo.getHeight()) / 2);
		batch.end();
		
		stage.draw();
	}
	
	@Override
	public void destroy(ViewController viewController) {
		
		//
		// Nothing to do here.
	}
}