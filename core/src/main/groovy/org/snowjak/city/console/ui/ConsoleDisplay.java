/**
 * 
 */
package org.snowjak.city.console.ui;

import static org.snowjak.city.util.Util.max;
import static org.snowjak.city.util.Util.min;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.snowjak.city.configuration.Configuration;
import org.snowjak.city.console.Console;
import org.snowjak.city.console.printers.AbstractPrinter;
import org.snowjak.city.console.printers.BasicPrinter;
import org.snowjak.city.console.printers.MethodPrinter;
import org.snowjak.city.console.printers.ThrowablePrinter;
import org.snowjak.city.console.printers.TypePrinter;
import org.snowjak.city.service.SkinService;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
	public static final int MAX_CONSOLE_ENTRIES = 1024;
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
	
	private final LinkedList<LinkedList<Actor>> consoleEntries = new LinkedList<>();
	
	private final Set<AbstractPrinter<?>> printers = new LinkedHashSet<>();
	private BasicPrinter basicPrinter;
	
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
					}
					
				}
				
				if (console.isHidden())
					return false;
				
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
					
					if (console.isHidden())
						stage.unfocusAll();
					else {
						stage.setKeyboardFocus(inputTextArea);
						stage.setScrollFocus(scrollPane);
					}
					
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
		
		basicPrinter = new BasicPrinter(this, skin);
		
		//
		// Add default printers
		printers.add(basicPrinter);
		printers.add(new ThrowablePrinter(this, skin));
		printers.add(new TypePrinter(this, skin));
		printers.add(new MethodPrinter(this, skin));
		
		consoleEntriesTable = new Table(skin);
		consoleEntriesTable.bottom().left();
		
		scrollPane = new ScrollPane(consoleEntriesTable);
		scrollPane.setScrollbarsVisible(true);
		scrollPane.setFadeScrollBars(false);
		
		inputTextArea = new ConsoleInputField(console, (c) -> console.execute(c),
				(c) -> { print("Trying to complete [" + c + "] ..."); }, "", skin);
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
	}
	
	/**
	 * Write the given values to the console and start a new line afterwards.
	 * 
	 * @param values
	 */
	public void println(Object... values) {
		
		print(values);
		newLine();
	}
	
	/**
	 * Write the given values to the console.
	 * 
	 * @param values
	 */
	public void print(Object... values) {
		
		for (Object value : values) {
			if (value instanceof String) {
				//
				// We need to account for newline characters in this String.
				final String string = (String) value;
				
				final String[] lines = string.split("\n");
				for (int i = 0; i < lines.length; i++) {
					
					//
					// If the last Actor on the current line is a Label,
					// let's append this text to it.
					//
					// Otherwise, we need to create a new Label for this line.
					//
					final Actor lastActor = getPreviousActor();
					if (lastActor != null && lastActor instanceof Label) {
						((Label) lastActor).getText().append(lines[i]);
						((Label) lastActor).invalidate();
					} else
						basicPrinter.print(lines[i]).forEach(this::appendToConsole);
					
					if (i < lines.length - 1)
						addNewConsoleLine();
				}
				
				if (string.endsWith("\n"))
					newLine();
				
			} else
				//
				// For anything that isn't a String, we can handle it more gracefully.
				// Just convert it using one of our printers, and add the resulting Actors to
				// the current line.
				getPrintFor(value).forEach(this::appendToConsole);
			
		}
	}
	
	/**
	 * End the current console-line and start a new one. Equivalent to sending
	 * {@link #writeToConsole(Object...) writeToConsole("\n")}.
	 */
	public void newLine() {
		
		addNewConsoleLine();
	}
	
	/**
	 * Remove old console-entries until the console is down to the right size.
	 */
	private void checkConsoleSize() {
		
		while (consoleEntries.size() >= MAX_CONSOLE_ENTRIES)
			consoleEntries.pop().forEach(Actor::remove);
	}
	
	/**
	 * Start a new line on the console and return it.
	 * 
	 * @return
	 */
	private Table addNewConsoleLine() {
		
		checkConsoleSize();
		
		final Table newLine = new Table();
		newLine.row().left().fill();
		
		consoleEntriesTable.row().left();
		consoleEntriesTable.add(newLine);
		
		scrollPane.setScrollPercentY(100);
		scrollPane.updateVisualScroll();
		
		consoleEntries.add(new LinkedList<>());
		
		return newLine;
	}
	
	/**
	 * Get the last Actor on the current line, or null if the current line is empty.
	 * 
	 * @return
	 */
	private Actor getPreviousActor() {
		
		if (consoleEntries.isEmpty() || consoleEntries.getLast().isEmpty())
			return null;
		
		return consoleEntries.getLast().getLast();
	}
	
	/**
	 * Append the given Actor to the end of the current line.
	 * <p>
	 * If {@code actor} is a Table, we automatically start a new line, make the
	 * Table its own line, and start another new line.
	 * </p>
	 * 
	 * @param actor
	 */
	private void appendToConsole(Actor actor) {
		
		if (actor instanceof Table) {
			
			if (consoleEntries.isEmpty()) {
				consoleEntriesTable.row().left();
				consoleEntriesTable.add(actor).fill();
				
				scrollPane.setScrollPercentY(100);
				scrollPane.updateVisualScroll();
				
				consoleEntries.add(new LinkedList<>());
				
				addNewConsoleLine();
			}
			
		} else {
			if (consoleEntries.isEmpty())
				addNewConsoleLine().add(actor);
			else {
				final int lastRowIndex = consoleEntries.size() - 1;
				
				final Table lastRow = (Table) consoleEntriesTable.getChild(lastRowIndex);
				lastRow.add(actor).fill();
			}
		}
		
		consoleEntries.getLast().add(actor);
	}
	
	/**
	 * Determine how the given object would print.
	 * <p>
	 * If any of the configured {@link AbstractPrinter}s
	 * {@link AbstractPrinter#canPrint(Object) can print} this object, then this
	 * method uses the first such printer it finds.
	 * </p>
	 * <p>
	 * Otherwise, it simply prints this object's {@link Object#toString()
	 * toString()}.
	 * </p>
	 * 
	 * @param <T>
	 * @param obj
	 * @return a list of Actors that together constitute the printed version of this
	 *         object
	 */
	@SuppressWarnings("unchecked")
	public <T> List<Actor> getPrintFor(T obj) {
		
		final List<Actor> result;
		if (obj instanceof CharSequence)
			result = basicPrinter.print((CharSequence) obj);
		else {
			final Optional<AbstractPrinter<?>> printer = printers.stream().filter(p -> p.canPrint(obj)).findAny();
			if (printer.isPresent()) {
				result = ((AbstractPrinter<T>) printer.get()).print(obj);
			} else
				result = basicPrinter.print(obj.toString());
		}
		
		return result;
	}
	
	private void adjustZoomLevel() {
		
		consoleEntries.forEach(l -> l.forEach(a -> a.setScale(getScale())));
	}
	
	/**
	 * Scaleable Actors should try to scale themselves by this multiplier.
	 * 
	 * @return
	 */
	public float getScale() {
		
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
	
	public Skin getSkin() {
		
		return skin;
	}
}
