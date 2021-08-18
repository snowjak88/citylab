/**
 * 
 */
package org.snowjak.city.console.ui;

import java.util.function.Consumer;

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
	private final Consumer<String> commandConsumer, completerConsumer;
	
	public ConsoleInputField(Console console, Consumer<String> commandConsumer, Consumer<String> completerConsumer,
			String text, Skin skin, String styleName) {
		
		super(text, skin, styleName);
		
		this.console = console;
		this.commandConsumer = commandConsumer;
		this.completerConsumer = completerConsumer;
	}
	
	public ConsoleInputField(Console console, Consumer<String> commandConsumer, Consumer<String> completerConsumer,
			String text, Skin skin) {
		
		super(text, skin);
		
		this.console = console;
		this.commandConsumer = commandConsumer;
		this.completerConsumer = completerConsumer;
	}
	
	public ConsoleInputField(Console console, Consumer<String> commandConsumer, Consumer<String> completerConsumer,
			String text, TextFieldStyle style) {
		
		super(text, style);
		
		this.console = console;
		this.commandConsumer = commandConsumer;
		this.completerConsumer = completerConsumer;
	}
	
	@Override
	protected InputListener createInputListener() {
		
		return new ConsoleInputFieldListener();
	}
	
	public class ConsoleInputFieldListener extends TextArea.TextAreaListener {
		
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
				
			} else if (event.getKeyCode() == Input.Keys.TAB) {
				completerConsumer.accept(getText());
				return true;
				
			} else if (event.getKeyCode() == console.getActivationCharacter()) {
				return false;
			}
			
			return super.keyTyped(event, character);
		}
	}
}
