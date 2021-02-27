/**
 * 
 */
package org.snowjak.city.controller;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.asset.AssetService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewController;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewInitializer;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewRenderer;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewResizer;
import com.github.czyzby.autumn.mvc.stereotype.View;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;
import com.github.czyzby.lml.parser.action.ActionContainer;

/**
 * Allows the user to interact with the map of the city.
 * 
 * @author snowjak88
 *
 */
@View(id = "cityMapScreen", value = "ui/templates/cityMapScreen.lml")
public class CityMapScreen implements ViewInitializer, ViewRenderer, ViewResizer, ActionContainer {
	
	private static final Logger LOG = LoggerService.forClass(CityMapScreen.class);
	
	@Inject
	private AssetService assetService;
	
	@Inject
	private InterfaceService interfaceService;
	
	private Viewport viewport = new FitViewport(8, 8);
	private SpriteBatch batch = new SpriteBatch();
	
	private TiledMap map = null;
	private IsometricTiledMapRenderer renderer;
	
	@Override
	public void initialize(Stage stage, ObjectMap<String, Actor> actorMappedByIds) {
		
		final TmxMapLoader loader = new TmxMapLoader(new InternalFileHandleResolver());
		
		final AssetManager assetManager = assetService.getEagerAssetManager();
		
		assetManager.setLoader(TiledMap.class, loader);
		
		assetManager.load("maps/untitled.tmx", TiledMap.class);
		assetManager.finishLoadingAsset("maps/untitled.tmx");
		map = assetManager.get("maps/untitled.tmx", TiledMap.class);
		
		renderer = new IsometricTiledMapRenderer(map, 1f / 64f, batch);
	}
	
	@Override
	public void render(Stage stage, float delta) {
		
		stage.act(delta);
		
		if (renderer != null) {
			viewport.apply();
			
			viewport.getCamera().update();
			renderer.setView((OrthographicCamera) viewport.getCamera());
			
			renderer.render();
			
			stage.getViewport().apply();
		}
		
		stage.draw();
	}
	
	@Override
	public void resize(Stage stage, int width, int height) {
		
		viewport.update(width, height);
	}
	
	@Override
	public void destroy(ViewController viewController) {
		
	}
}
