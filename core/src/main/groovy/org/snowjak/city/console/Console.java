/**
 * 
 */
package org.snowjak.city.console;

import org.snowjak.city.configuration.InitPriority;
import org.snowjak.city.console.executors.AbstractConsoleExecutor;
import org.snowjak.city.console.executors.StubExecutor;
import org.snowjak.city.console.ui.ConsoleDisplay;
import org.snowjak.city.service.SkinService;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;

/**
 * Interface for the game console.
 * <p>
 * Ensure that you hook this into your primary event-loop <em>after</em> all
 * your other display-elements render -- e.g.:
 * 
 * <pre>
 * ...
 * 
 * public void render(float delta) {
 * 
 *     yourStage.act(delta);
 *     yourStage.render();
 *     
 *     console.act(delta);
 *     console.render();
 * 
 * };
 * 
 * ...
 * </pre>
 * </p>
 * 
 * @author snowjak88
 *
 */
@Component
public class Console {
	
	/**
	 * Default activation-character is {@link Input.Keys#GRAVE GRAVE} ({@code ` }).
	 * This should work just fine for US-QWERTY-layout keyboards. Don't have a plan
	 * yet for non-US layouts.
	 */
	public static final int DEFAULT_ACTIVATION_CHARACTER = Input.Keys.GRAVE;
	
	private boolean isReady = false;
	private boolean isHidden = true;
	private int activationCharacter = DEFAULT_ACTIVATION_CHARACTER;
	
	private final ConsoleDisplay display;
	private final AbstractConsoleExecutor executor;
	
	public Console(SkinService skinService, Viewport viewport) {
		
		this.display = new ConsoleDisplay(this, skinService, viewport);
		this.executor = new StubExecutor(this);
	}
	
	@Initiate(priority = InitPriority.LOW_PRIORITY)
	public void init() {
		
		display.init();
		isReady = true;
	}
	
	/**
	 * Indicates if this Console has been initialized yet.
	 * 
	 * @return
	 */
	public boolean isReady() {
		
		return isReady;
	}
	
	/**
	 * Write the given text to the console.
	 * 
	 * @param message
	 */
	public void write(String message) {
		
		this.display.addConsoleEntry(message);
	}
	
	/**
	 * Submit the given text to the configured console-executor.
	 * 
	 * @param commandText
	 */
	public void execute(String commandText) {
		
		if (executor != null)
			executor.execute(commandText);
	}
	
	public void act(float delta) {
		
		display.act(delta);
	}
	
	public void render() {
		
		display.render();
	}
	
	public void resize(int width, int height) {
		
		display.resize(width, height);
	}
	
	public InputProcessor getInputProcessor() {
		
		return display.getInputProcessor();
	}
	
	public int getActivationCharacter() {
		
		return activationCharacter;
	}
	
	public void setActivationCharacter(int activationCharacter) {
		
		this.activationCharacter = activationCharacter;
	}
	
	public boolean isHidden() {
		
		return isHidden;
	}
	
	public void setHidden(boolean isHidden) {
		
		this.isHidden = isHidden;
	}
	
	public void toggleHidden() {
		
		isHidden = !isHidden;
	}
}
