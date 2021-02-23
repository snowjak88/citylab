/**
 * 
 */
package org.snowjak.city.controller;

import java.util.Arrays;

import org.snowjak.city.CityGame;
import org.snowjak.city.map.tileset.TilesetException;
import org.snowjak.city.service.TilesetService;
import org.snowjak.city.service.TilesetService.TilesetDomain;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.github.czyzby.autumn.annotation.Dispose;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewRenderer;
import com.github.czyzby.autumn.mvc.stereotype.View;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.github.czyzby.lml.util.LmlUtilities;

/**
 * @author snowjak88
 *
 */
@View(id = "mainScreen", value = "ui/templates/mainScreen.lml")
public class MainScreenController implements ViewRenderer, ActionContainer {
	
	@Inject
	private InterfaceService interfaceService;
	
	@LmlActor("mainScreenContainer")
	private Container<Actor> mainScreenContainer;
	
	@Dispose
	private final SpriteBatch batch = new SpriteBatch();
	
	@Inject
	private TilesetService tilesets;
	
	@Override
	public void render(Stage stage, float delta) {
		
		stage.act();
		
		batch.begin();
		
		int x = 0, y = 0;
		for (TiledMapTile tile : tilesets.getTilesetFor(TilesetDomain.WORLD)) {
			
			batch.draw(tile.getTextureRegion(), x, y + tilesets.getTileDescriptorFor(tile).offset);
			
			x += tilesets.getDescriptorFor(TilesetDomain.WORLD).baseWidth;
			if (x >= CityGame.WIDTH) {
				x = 0;
				y += tilesets.getDescriptorFor(TilesetDomain.WORLD).baseHeight;
			}
		}
		
		batch.end();
		
		stage.draw();
	}
	
	@LmlAction
	public void updateTileset(final Actor actor) {
		
		final Logger log = LoggerService.forClass(MainScreenController.class);
		
		try {
			tilesets.rebuildTileset(TilesetDomain.valueOf(LmlUtilities.getActorId(actor)));
		} catch (TilesetException e) {
			log.error("Could not load tileset for some reason.", e);
		}
	}
	
	@LmlAction
	public String[] getDomains() {
		
		return Arrays.stream(TilesetDomain.values()).map(d -> d.name()).toArray(len -> new String[len]);
	}
}
