/**
 * 
 */
package org.snowjak.city.tools

import org.snowjak.city.service.GameService
import org.snowjak.city.tools.activation.ActivationMethod
import org.snowjak.city.tools.activation.ButtonActivationMethod
import org.snowjak.city.tools.activation.KeyActivationMethod
import org.snowjak.city.tools.activation.MenuActivationMethod
import org.snowjak.city.tools.groups.ButtonToolGroup
import org.snowjak.city.tools.groups.MenuToolGroup

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
	
	final String id
	
	String title = "", description = ""
	
	private final FileHandle baseDirectory
	
	private final GameService gameService
	private final Map<String,MenuToolGroup> menuGroups = [:]
	private final Map<String,ButtonToolGroup> buttonGroups = [:]
	
	final Set<ActivationMethod> activationMethods = new LinkedHashSet<>()
	
	public Tool(String id, FileHandle baseDirectory, GameService gameService) {
		this.id = id
		this.baseDirectory = baseDirectory
		this.gameService = gameService
	}
	
	//
	//
	//
	
	public void activationKey(String id, @DelegatesTo(value=KeyActivationMethod, strategy=Closure.DELEGATE_FIRST) Closure activationSpec) {
		
		final activation = new KeyActivationMethod(id, { -> this.activate(gameService)})
		activationSpec = activationSpec.rehydrate(activation, this, this)
		activationSpec.resolveStrategy = Closure.DELEGATE_FIRST
		activationSpec()
		
		activationMethods << activation
	}
	
	public void activationButton(String id, @DelegatesTo(value=ButtonActivationMethod, strategy=Closure.DELEGATE_FIRST) Closure activationSpec) {
		// TODO
		final activation = new ButtonActivationMethod(id, buttonGroups, baseDirectory, { -> this.activate(gameService)})
		activationSpec = activationSpec.rehydrate(activation, this, this)
		activationSpec.resolveStrategy = Closure.DELEGATE_FIRST
		activationSpec()
		
		activationMethods << activation
	}
	
	public void activationMenu(String id, @DelegatesTo(value=MenuActivationMethod, strategy=Closure.DELEGATE_FIRST) Closure activationSpec) {
		// TODO
		final activation = new MenuActivationMethod(id, buttonGroups, { -> this.activate(gameService)})
		activationSpec = activationSpec.rehydrate(activation, this, this)
		activationSpec.resolveStrategy = Closure.DELEGATE_FIRST
		activationSpec()
		
		activationMethods << activation
	}
	
	//
	//
	//
	
	public MenuToolGroup menuGroup(String id) {
		if(!menuGroups.containsKey(id))
			throw new IllegalArgumentException("Cannot reference menu tool-group '$id' before it is defined!")
		menuGroups[id]
	}
	
	public MenuToolGroup menuGroup(String id, @DelegatesTo(value=MenuToolGroup, strategy=Closure.DELEGATE_FIRST) Closure groupSpec) {
		final group = new MenuToolGroup(id, menuGroups, baseDirectory)
		groupSpec = groupSpec.rehydrate(group, this, this)
		groupSpec.resolveStrategy = Closure.DELEGATE_FIRST
		groupSpec()
		
		menuGroups[group.id] = group
		group
	}
	
	public ButtonToolGroup buttonGroup(String id) {
		if(!buttonGroups.containsKey(id))
			throw new IllegalArgumentException("Cannot reference button tool-group '$id' before it is defined!")
		buttonGroups[id]
	}
	
	public ButtonToolGroup buttonGroup(String id, @DelegatesTo(value=ButtonToolGroup, strategy=Closure.DELEGATE_FIRST) Closure groupSpec) {
		final group = new ButtonToolGroup(id, buttonGroups, baseDirectory)
		groupSpec = groupSpec.rehydrate(group, this, this)
		groupSpec.resolveStrategy = Closure.DELEGATE_FIRST
		groupSpec()
		
		buttonGroups[id] = group
		group
	}
	
	//
	//
	//
	
	public void activate(GameService service) {
		throw new UnsupportedOperationException()
	}
}
