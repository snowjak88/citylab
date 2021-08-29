/**
 * 
 */
package org.snowjak.city.console.ui;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.snowjak.city.console.Console;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;

/**
 * Customization of {@link TextArea}. Overrides default behavior for the given
 * keys:
 * <p>
 * <ul>
 * <li>[Enter]: submits the whole command-field to the configured
 * {@link Consumer "command consumer"}</li>
 * <li>[Tab]: submits the whole command-field to the configured {@link Consumer
 * "completer consumer"}</li>
 * <li>[activation-character]: ignores this character</li>
 * </ul>
 * </p>
 * 
 * @author snowjak88
 *
 */
public class ConsoleInputField extends TextArea {
	
	private final Console console;
	private final Consumer<String> commandConsumer;
	private final BiConsumer<InputEvent, String> completerConsumer;
	private final Supplier<String> previousHistorySupplier;
	private final Supplier<String> nextHistorySupplier;
	
	public ConsoleInputField(Console console, Consumer<String> commandConsumer,
			Supplier<String> previousHistorySupplier, Supplier<String> nextHistorySupplier,
			BiConsumer<InputEvent, String> completerConsumer, String text, Skin skin, String styleName) {
		
		super(text, skin, styleName);
		
		this.console = console;
		this.commandConsumer = commandConsumer;
		this.completerConsumer = completerConsumer;
		this.previousHistorySupplier = previousHistorySupplier;
		this.nextHistorySupplier = nextHistorySupplier;
	}
	
	public ConsoleInputField(Console console, Consumer<String> commandConsumer,
			Supplier<String> previousHistorySupplier, Supplier<String> nextHistorySupplier,
			BiConsumer<InputEvent, String> completerConsumer, String text, Skin skin) {
		
		super(text, skin);
		
		this.console = console;
		this.commandConsumer = commandConsumer;
		this.completerConsumer = completerConsumer;
		this.previousHistorySupplier = previousHistorySupplier;
		this.nextHistorySupplier = nextHistorySupplier;
	}
	
	public ConsoleInputField(Console console, Consumer<String> commandConsumer,
			Supplier<String> previousHistorySupplier, Supplier<String> nextHistorySupplier,
			BiConsumer<InputEvent, String> completerConsumer, String text, TextFieldStyle style) {
		
		super(text, style);
		
		this.console = console;
		this.commandConsumer = commandConsumer;
		this.completerConsumer = completerConsumer;
		this.previousHistorySupplier = previousHistorySupplier;
		this.nextHistorySupplier = nextHistorySupplier;
	}
	
	@Override
	protected InputListener createInputListener() {
		
		return new ConsoleInputFieldListener();
	}
	
	public class ConsoleInputFieldListener extends TextArea.TextAreaListener {
		
		private int ctrlCount = 0;
		
		@Override
		public boolean keyDown(InputEvent event, int keycode) {
			
			if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT)
				ctrlCount++;
			
			else if (keycode == Input.Keys.UP) {
				setText(previousHistorySupplier.get());
				return true;
				
			} else if (keycode == Input.Keys.DOWN) {
				setText(nextHistorySupplier.get());
				return true;
				
			}
			
			return super.keyDown(event, keycode);
		}
		
		@Override
		public boolean keyUp(InputEvent event, int keycode) {
			
			if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT)
				ctrlCount--;
			
			return super.keyUp(event, keycode);
		}
		
		@Override
		public boolean keyTyped(InputEvent event, char character) {
			
			if (isDisabled())
				return false;
			
			if (!hasKeyboardFocus())
				return false;
			
			if (event.getKeyCode() == Input.Keys.ENTER || event.getKeyCode() == Input.Keys.NUMPAD_ENTER) {
				commandConsumer.accept(getText());
				setText("");
				return true;
				
			} else if (event.getKeyCode() == Input.Keys.SPACE && ctrlCount > 0) {
				completerConsumer.accept(event, getText());
				return true;
				
			} else if (event.getKeyCode() == console.getActivationCharacter()) {
				return false;
			}
			
			return super.keyTyped(event, character);
		}
	}
}
