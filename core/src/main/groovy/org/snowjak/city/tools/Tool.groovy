/**
 * 
 */
package org.snowjak.city.tools

import java.util.function.Consumer

import org.snowjak.city.input.hotkeys.Hotkey
import org.snowjak.city.service.GameService

import com.badlogic.gdx.files.FileHandle

/**
 * A Tool defines a mechanism whereby the user can affect the world.
 * <p>
 * Only one Tool may be active at any time. A Tool is activated, either by the user clicking
 * on a GUI element -- a button, or an item from a menu-bar -- or by an activation-key.
 * </p>
 * <p>
 * A Tool can define several things:
 * <ul>
 * <li>At least one activation-method (a button + button-group, a menu-bar item, a key-combination)</li>
 * <li>At least one "is-active" handler -- called every frame so long as the Tool is active</li>
 * <li>At least oneF "use" handler -- called when the user clicks on the map (whether as a single-click or drag)</li>
 * </ul>
 * </p>
 * @author snowjak88
 *
 */
class Tool {
	
	/**
	 * Add a Consumer for this Tool if you want to be notified whenever {@link #enabled} is changed.
	 */
	final Set<Consumer<Tool>> enabledListeners = Collections.synchronizedSet(new LinkedHashSet<>())
	
	final String id
	
	boolean enabled = true
	
	private final FileHandle baseDirectory
	
	private final GameService gameService
	private final Map<String,ToolGroup> groups = new LinkedHashMap<>()
	private final Map<String,ToolButton> buttons = new LinkedHashMap<>()
	private final Map<String,Hotkey> hotkeys = new LinkedHashMap<>()
	
	public Tool(String id, FileHandle baseDirectory, Map<String,ToolGroup> toolGroups, GameService gameService) {
		this.id = id
		this.baseDirectory = baseDirectory
		this.groups.putAll toolGroups
		this.gameService = gameService
	}
	
	public void button(String id, @DelegatesTo(value=ToolButton, strategy=Closure.DELEGATE_FIRST) Closure buttonSpec) {
		final button = new ToolButton(this, id, baseDirectory)
		buttonSpec = buttonSpec.rehydrate(button, this, this)
		buttonSpec.resolveStrategy = Closure.DELEGATE_FIRST
		buttonSpec()
		
		buttons << [ "$id" : button ]
	}
	
	public void key(String id, @DelegatesTo(value=Hotkey, strategy=Closure.DELEGATE_FIRST) Closure hotkeySpec) {
		final Hotkey key = new Hotkey(id)
		hotkeySpec = hotkeySpec.rehydrate(key, this, this)
		hotkeySpec.resolveStrategy = Closure.DELEGATE_FIRST
		hotkeySpec()
		
		hotkeys << [ "$id" : key ]
	}
	
	
	public void setEnabled(boolean enabled) {
		
		this.enabled = enabled;
		enabledListeners.forEach { it.accept thisObject }
	}
	
	public void activate() {
		println "Tool [$id] activated!"
	}
}
