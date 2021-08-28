/**
 * 
 */
package org.snowjak.city.screens;

import org.snowjak.city.configuration.Configuration;
import org.snowjak.city.console.Console;
import org.snowjak.city.screens.menupages.GameSetupMenuPage;
import org.snowjak.city.screens.menupages.MainMenuPage;
import org.snowjak.city.service.GameService;
import org.snowjak.city.service.I18NService;
import org.snowjak.city.service.SkinService;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;

/**
 * Presents the main-menu.
 * <p>
 * Provides 2 primary elements:
 * <ul>
 * <li>A list of menu-items on the left-hand side</li>
 * <li>A content-pane (presenting whatever menu-item is currently selected)</li>
 * </ul>
 * </p>
 * 
 * @author snowjak88
 *
 */
@Component
public class MainMenuScreen extends AbstractGameScreen {
	
	private final SkinService skinService;
	
	@Inject
	private I18NService i18nService;
	
	@Inject
	private LoadingScreen loadingScreen;
	
	@Inject
	private GameScreen gameScreen;
	
	private final Container<Actor> pageContainer = new Container<>();
	private MainMenuPage currentPage = null;
	
	private final GameSetupMenuPage gameSetupMenuPage;
	
	public MainMenuScreen(GameService gameService, Console console, SkinService skinService, Stage stage,
			GameSetupMenuPage gameSetupMenuPage) {
		
		super(gameService, console, skinService, stage);
		this.skinService = skinService;
		this.gameSetupMenuPage = gameSetupMenuPage;
	}
	
	@Override
	protected Actor getRoot() {
		
		final VerticalGroup leftHandMenuGroup = new VerticalGroup();
		leftHandMenuGroup.columnCenter().padTop(200).padLeft(100).space(15);
		
		leftHandMenuGroup.addActor(getMenuButton("menu-gamesetup", gameSetupMenuPage));
		leftHandMenuGroup.addActor(getMenuButton("menu-quit", () -> Gdx.app.exit()));
		
		final Table root = new Table();
		root.center().setFillParent(true);
		root.add(leftHandMenuGroup).growY().center();
		root.add(pageContainer).grow().center();
		
		gameSetupMenuPage.setOnGameStart(() -> {
			loadingScreen
					.setLoadingTasks(getGameService().getNewGameLoadingTask(gameSetupMenuPage.getNewGameParameters()));
			loadingScreen.setLoadingCompleteAction(() -> loadingScreen.changeScreen(gameScreen));
			changeScreen(loadingScreen);
		});
		
		return root;
	}
	
	@Override
	public void beforeStageAct(float delta) {
		
	}
	
	@Override
	public void renderBeforeStage(float delta) {
		
	}
	
	@Override
	public void renderAfterStage(float delta) {
		
	}
	
	private TextButton getMenuButton(String textKey, MainMenuPage page) {
		
		final MainMenuScreen thisScreen = this;
		
		final Skin skin = skinService.getSkin(Configuration.SKIN_NAME);
		
		final TextButton button = new TextButton(i18nService.get(textKey), skin);
		button.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				pageContainer.addAction(Actions.run(() -> {
					if (currentPage != null)
						currentPage.hide();
					
					final Actor pageRoot = page.getRoot(thisScreen);
					pageContainer.setActor(pageRoot);
					
					currentPage = page;
					currentPage.show();
				}));
			}
		});
		return button;
	}
	
	private TextButton getMenuButton(String textKey, Runnable action) {
		
		final Skin skin = skinService.getSkin(Configuration.SKIN_NAME);
		
		final TextButton button = new TextButton(i18nService.get(textKey), skin);
		button.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				action.run();
			}
		});
		return button;
	}
}
