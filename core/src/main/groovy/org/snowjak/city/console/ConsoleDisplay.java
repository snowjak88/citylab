/**
 * 
 */
package org.snowjak.city.console;

import static org.snowjak.city.util.Util.max;
import static org.snowjak.city.util.Util.min;

import java.util.LinkedList;

import org.snowjak.city.configuration.Configuration;
import org.snowjak.city.service.SkinService;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * @author snowjak88
 *
 */
public class ConsoleDisplay {
	
	/**
	 * Console will display at most N lines of history.
	 */
	public static final int MAX_CONSOLE_ENTRIES = 64;
	/**
	 * Console will permit at most N characters of input.
	 */
	public static final int CONSOLE_INPUT_BUFFER_LENGTH = 4096;
	
	/**
	 * Console will permit zooming-in to the given factor (1.0 == "normal size", 2.0
	 * == "half size")
	 */
	public static final float MINIMUM_ZOOM = 0.5f,
			/**
			 * Console will permit zooming-out to the given factor (1.0 == "normal size",
			 * 2.0 == "half size")
			 */
			MAXIMUM_ZOOM = 2f;
	
	private final Console console;
	private final SkinService skinService;
	
	private Stage stage;
	
	private Skin skin;
	private Table root;
	private TextArea inputTextArea;
	private ScrollPane scrollPane;
	private Table consoleEntriesTable;
	
	private final LinkedList<Label> consoleLines = new LinkedList<>();
	private Label currentConsoleLine;
	
	private float zoom = 1;
	
	public ConsoleDisplay(Console console, SkinService skinService, Viewport viewport) {
		
		this.console = console;
		this.skinService = skinService;
		
		stage = new Stage(viewport);
		
		// Add a listener for the console's activation-character,
		// and ignore all other input-events if the console is not currently visible.
		//
		stage.addListener(new InputListener() {
			
			private boolean isCtrl = false;
			
			@Override
			public boolean handle(Event e) {
				
				if (e instanceof InputEvent) {
					final InputEvent event = (InputEvent) e;
					
					switch (event.getType()) {
					case keyTyped:
						return keyTyped(event, event.getCharacter());
					case keyDown:
						return keyDown(event, event.getKeyCode());
					case keyUp:
						return keyUp(event, event.getKeyCode());
					case scrolled:
						return scrolled(event, event.getStageX(), event.getStageY(), event.getScrollAmountX(),
								event.getScrollAmountY());
					default:
						break;
					}
					
					if (console.isHidden())
						return false;
					
				}
				
				return super.handle(e);
			}
			
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				
				if (console.isHidden())
					return false;
				
				if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT)
					isCtrl = true;
				
				return super.keyDown(event, keycode);
			}
			
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				
				if (console.isHidden())
					return false;
				
				if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT)
					isCtrl = false;
				
				return super.keyUp(event, keycode);
			}
			
			@Override
			public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
				
				if (console.isHidden())
					return false;
				
				if (isCtrl) {
					zoom = min(max(zoom + Math.signum(amountY) * 0.1f, MINIMUM_ZOOM), MAXIMUM_ZOOM);
					adjustZoomLevel();
				}
				
				return super.scrolled(event, x, y, amountX, amountY);
			}
			
			@Override
			public boolean keyTyped(InputEvent event, char character) {
				
				if (event.getKeyCode() == console.getActivationCharacter()) {
					console.toggleHidden();
					stage.getRoot().setVisible(!console.isHidden());
					event.cancel();
					
					return true;
				}
				
				if (console.isHidden())
					return false;
				
				return super.keyTyped(event, character);
			}
		});
	}
	
	public void init() {
		
		skin = skinService.getSkin(Configuration.SKIN_NAME);
		
		consoleEntriesTable = new Table(skin);
		consoleEntriesTable.bottom().left();
		
		scrollPane = new ScrollPane(consoleEntriesTable);
		scrollPane.setScrollingDisabled(true, false);
		scrollPane.setScrollbarsVisible(true);
		scrollPane.setFadeScrollBars(false);
		
		inputTextArea = new ConsoleInputField(console, (c) -> console.execute(c),
				(c) -> { addConsoleEntry("Trying to complete [" + c + "] ..."); }, "", skin);
		inputTextArea.setMaxLength(4096);
		inputTextArea.setPrefRows(1.5f);
		inputTextArea.setFocusTraversal(false);
		
		root = new Table(skin);
		root.row().grow();
		root.add(scrollPane);
		root.row().growX();
		root.add(inputTextArea);
		
		if (skin.has("console-background", Drawable.class))
			root.setBackground("console-background");
		
		root.setFillParent(true);
		
		stage.getRoot().addActor(root);
		stage.setKeyboardFocus(inputTextArea);
	}
	
	public void addConsoleEntry(String text) {
		
		nextConsoleLine();
		
		currentConsoleLine.setText(text);
	}
	
	public void appendConsoleEntry(String text) {
		
		if (currentConsoleLine == null)
			nextConsoleLine();
		
		currentConsoleLine.setText(currentConsoleLine.getText().append(text));
	}
	
	private void nextConsoleLine() {
		
		if (consoleEntriesTable.getRows() >= MAX_CONSOLE_ENTRIES)
			consoleLines.pop().remove();
		
		currentConsoleLine = new Label("", skin, "console");
		currentConsoleLine.setWrap(true);
		currentConsoleLine.setFontScale(getFontScale());
		
		consoleEntriesTable.row().growX();
		consoleEntriesTable.add(currentConsoleLine);
		consoleLines.addLast(currentConsoleLine);
		
		scrollPane.setScrollPercentY(100);
		scrollPane.updateVisualScroll();
	}
	
	private void adjustZoomLevel() {
		
		for (Label consoleEntry : consoleLines)
			consoleEntry.setFontScale(getFontScale());
	}
	
	private float getFontScale() {
		
		return 1f / zoom;
	}
	
	public void act(float delta) {
		
		stage.act(delta);
	}
	
	public void render() {
		
		if (!console.isHidden())
			stage.draw();
	}
	
	/**
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		
		stage.getViewport().update(width, height, true);
	}
	
	/**
	 * @return this ConsoleDisplay's {@link InputProcessor} (i.e., its
	 *         {@link Stage})
	 */
	public InputProcessor getInputProcessor() {
		
		return stage;
	}
}
