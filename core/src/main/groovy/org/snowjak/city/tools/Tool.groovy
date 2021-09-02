/**
 * 
 */
package org.snowjak.city.tools

import java.util.function.Consumer

import org.snowjak.city.input.hotkeys.Hotkey
import org.snowjak.city.module.Module
import org.snowjak.city.service.GameService
import org.snowjak.city.tools.activity.Activity

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
	
	String title
	boolean enabled = true
	
	final ToolActivities active
	
	private final Module module
	private final FileHandle baseDirectory
	private final GameService gameService
	
	final Map<String,ToolGroup> groups = new LinkedHashMap<>()
	final Map<String,ToolButton> buttons = new LinkedHashMap<>()
	final Map<String,Hotkey> hotkeys = new LinkedHashMap<>()
	final Set<Activity> activities = new LinkedHashSet<>()
	final Set<Runnable> inactivities = new LinkedHashSet<>()
	
	public Tool(String id, Module module, FileHandle baseDirectory, Map<String,ToolGroup> toolGroups, GameService gameService) {
		this.id = id
		this.module = module
		this.baseDirectory = baseDirectory
		this.groups.putAll toolGroups
		this.gameService = gameService
		this.active = new ToolActivities(this, gameService)
	}
	
	def propertyMissing(name) {
		module."$name"
	}
	
	def propertyMissing(name, value) {
		module."$name" = value
	}
	
	public void button(String id, @DelegatesTo(value=ToolButton, strategy=Closure.DELEGATE_FIRST) Closure buttonSpec) {
		final button = new ToolButton(this, id, baseDirectory)
		buttonSpec = buttonSpec.rehydrate(button, module, this)
		buttonSpec.resolveStrategy = Closure.DELEGATE_FIRST
		buttonSpec()
		
		buttons << [ "$id" : button ]
	}
	
	public void key(String id, @DelegatesTo(value=Hotkey, strategy=Closure.DELEGATE_FIRST) Closure hotkeySpec) {
		final Hotkey hotkey = new Hotkey(id)
		hotkeySpec = hotkeySpec.rehydrate(hotkey, module, this)
		hotkeySpec.resolveStrategy = Closure.DELEGATE_FIRST
		hotkeySpec()
		
		hotkeys << [ "$id" : hotkey ]
	}
	
	/**
	 * Execute this action when this Tool is deactivated.
	 * 
	 * @param inactiveAction
	 */
	public void inactive(Closure inactiveAction) {
		inactiveAction.resolveStrategy = Closure.DELEGATE_FIRST
		inactiveAction.delegate = this
		
		inactivities << inactiveAction
	}
	
	//
	//
	//
	
	public void setEnabled(boolean enabled) {
		
		this.enabled = enabled;
		enabledListeners.forEach { it.accept thisObject }
	}
	
	public void activate() {
		
		gameService.state.activeTool?.deactivate()
		gameService.state.activeTool = this
		
		if(enabled)
			activities.each { it.activate() }
	}
	
	public void update() {
		
		if(enabled)
			activities.each { it.update() }
	}
	
	public void deactivate() {
		
		inactivities.each { it.run() }
		activities.each { it.deactivate() }
		gameService.state.activeTool = null
	}
}
