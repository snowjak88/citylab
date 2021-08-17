/**
 * 
 */
package org.snowjak.city.screens;

import org.snowjak.city.screens.loadingtasks.NewGameSetupTask;
import org.snowjak.city.screens.menupages.GameSetupMenuPage;
import org.snowjak.city.screens.menupages.MainMenuPage;
import org.snowjak.city.service.I18NService;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.kotcrab.vis.ui.widget.VisTextButton;

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
	
	@Inject
	private I18NService i18nService;
	
	@Inject
	private GameSetupMenuPage gameSetupMenuPage;
	
	@Inject
	private LoadingScreen loadingScreen;
	
	@Inject
	private NewGameSetupTask newGameSetupLoadingTask;
	
	@Inject
	private GameScreen gameScreen;
	
	private final Container<Actor> pageContainer = new Container<>();
	private MainMenuPage currentPage = null;
	
	public MainMenuScreen(Stage stage) {
		
		super(stage);
	}
	
	@Override
	protected Actor getRoot() {
		
		final VerticalGroup leftHandMenuGroup = new VerticalGroup();
		leftHandMenuGroup.align(Align.right);
		
		pageContainer.fill();
		
		final HorizontalGroup root = new HorizontalGroup();
		root.setFillParent(true);
		root.align(Align.center);
		root.addActor(leftHandMenuGroup);
		root.addActor(pageContainer);
		
		leftHandMenuGroup.addActor(getMenuButton("menu-gamesetup", gameSetupMenuPage));
		leftHandMenuGroup.addActor(getMenuButton("menu-quit", () -> Gdx.app.exit()));
		
		gameSetupMenuPage.setOnGameStart(() -> {
			loadingScreen.setLoadingTasks(newGameSetupLoadingTask);
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
	
	private VisTextButton getMenuButton(String textKey, MainMenuPage page) {
		
		final MainMenuScreen thisScreen = this;
		
		return new VisTextButton(i18nService.get(textKey), new ChangeListener() {
			
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
	}
	
	private VisTextButton getMenuButton(String textKey, Runnable action) {
		
		return new VisTextButton(i18nService.get(textKey), new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				action.run();
			}
		});
	}
}
