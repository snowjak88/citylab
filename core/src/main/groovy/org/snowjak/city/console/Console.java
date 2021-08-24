/**
 * 
 */
package org.snowjak.city.console;

import java.util.Collections;
import java.util.List;

import org.snowjak.city.configuration.InitPriority;
import org.snowjak.city.console.completers.ConsoleWordCompleter;
import org.snowjak.city.console.completers.StubWordCompleter;
import org.snowjak.city.console.executors.AbstractConsoleExecutor;
import org.snowjak.city.console.executors.GroovyConsoleExecutor;
import org.snowjak.city.console.model.ConsoleModel;
import org.snowjak.city.console.ui.ConsoleDisplay;
import org.snowjak.city.service.GameAssetService;
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
	private final ConsoleWordCompleter completer;
	private final AbstractConsoleExecutor executor;
	
	public Console(GameAssetService assetService, SkinService skinService, Viewport viewport) {
		
		this.display = new ConsoleDisplay(this, skinService, viewport);
		this.completer = new StubWordCompleter();
		this.executor = new GroovyConsoleExecutor(this, new ConsoleModel(assetService), new ConsolePrintStream(this));
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
	
	public void println(Object... values) {
		
		if (isReady())
			display.println(values);
	}
	
	public void print(Object... values) {
		
		if (isReady())
			display.print(values);
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
	
	/**
	 * Run the given text through the configured command-completer.
	 * 
	 * @param commandText
	 * @return
	 */
	public List<String> complete(String commandText) {
		
		if (completer != null)
			return completer.complete(commandText);
		
		return Collections.emptyList();
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
