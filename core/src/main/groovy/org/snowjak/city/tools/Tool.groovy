/**
 * 
 */
package org.snowjak.city.tools

import static org.snowjak.city.module.ModuleExceptionRegistry.FailureDomain.TOOL_DEACTIVATE
import static org.snowjak.city.module.ModuleExceptionRegistry.FailureDomain.TOOL_UPDATE

import java.util.function.Consumer

import org.snowjak.city.input.InputEventReceiver
import org.snowjak.city.input.MapClickEvent
import org.snowjak.city.input.MapDragEndEvent
import org.snowjak.city.input.MapDragStartEvent
import org.snowjak.city.input.MapDragUpdateEvent
import org.snowjak.city.input.MapHoverEvent
import org.snowjak.city.input.hotkeys.Hotkey
import org.snowjak.city.module.Module
import org.snowjak.city.module.ModuleExceptionRegistry.FailureDomain
import org.snowjak.city.service.GameService
import org.snowjak.city.tools.activity.Activity
import org.snowjak.city.tools.activity.InputReceivingActivity

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
	final Set<Consumer<Tool>> enabledListeners = Collections.synchronizedSet(new LinkedHashSet<>()),
	/**
	 * Add a Consumer for this Tool if you want to be notified whenever {@link #activate()} is called.
	 */
	activateListeners = Collections.synchronizedCollection(new LinkedHashSet<>()),
	/**
	 * Add a Consumer for this Tool if you want to be notified whenever {@link #deactivate()} is called.
	 */
	deactivateListeners = Collections.synchronizedCollection(new LinkedHashSet<>())
	
	final String id
	
	String title
	boolean enabled = true
	
	final Module module
	private final Binding binding = new Binding()
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
		module.binding.variables.each { n,v -> this.binding[n] = v }
		this.baseDirectory = baseDirectory
		this.groups.putAll toolGroups
		this.gameService = gameService
		
		Tool.metaClass.methodMissing = { name, args ->
			getProperty(name).call(*args)
		}
	}
	
	def propertyMissing(name) {
		if(binding.hasVariable(name))
			return binding[name]
		module.getProperty(name)
	}
	
	def propertyMissing(name, value) {
		binding[name] = value
	}
	
	public void button(String id, @DelegatesTo(value=ToolButton, strategy=Closure.DELEGATE_FIRST) Closure buttonSpec) {
		final button = new ToolButton(this, id, baseDirectory)
		buttonSpec = buttonSpec.rehydrate(button, this, this)
		buttonSpec.resolveStrategy = Closure.DELEGATE_FIRST
		buttonSpec()
		
		buttons << [ "$id" : button ]
	}
	
	public void key(String id, @DelegatesTo(value=Hotkey, strategy=Closure.DELEGATE_FIRST) Closure hotkeySpec) {
		final Hotkey hotkey = new Hotkey(id)
		hotkeySpec = hotkeySpec.rehydrate(hotkey, this, this)
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
	
	/**
	 * Register a map-hover activity. While the associated tool is active, this activity will be
	 * called every frame that <em>none</em> of the cursor's buttons are depressed. This activity will be
	 * passed the coordinates of the map-cell that the cursor is over. (<strong>Note</strong> that the given map-cell may
	 * not be a valid cell at all!)
	 * @param mapHoverSpec
	 */
	public void mapHover(@DelegatesTo(value=MapCoordReceiver, strategy=Closure.DELEGATE_FIRST) Closure mapHoverSpec) {
		
		mapHoverSpec.delegate = this
		mapHoverSpec.resolveStrategy = Closure.DELEGATE_FIRST
		final mapHoverReceiver = mapHoverSpec as MapCoordReceiver
		final adapter = [ receive: { MapHoverEvent e ->
				mapHoverReceiver.receive e.x, e.y
			} ] as InputEventReceiver<MapHoverEvent>
		
		final activity = new InputReceivingActivity(this, gameService, MapHoverEvent, adapter)
		activities << activity
	}
	
	/**
	 * Register a map-click activity. While the associated tool is active, this activity will
	 * be called whenever the user clicks a map-cell with the specified mouse-button. This activity will
	 * be passed the coordinates of the map-cell that was clicked.
	 * (<strong>Note</strong> that the given map-cell may not be a valid cell at all!)
	 * @param button c.f. {@link Input.Buttons}
	 * @param mapClickSpec
	 */
	public void mapClick(int button, @DelegatesTo(value=MapCoordReceiver, strategy=Closure.DELEGATE_FIRST) Closure mapClickSpec) {
		
		mapClickSpec.delegate = this
		mapClickSpec.resolveStrategy = Closure.DELEGATE_FIRST
		final mapClickReceiver = mapClickSpec as MapCoordReceiver
		
		final requiredButton = button
		final adapter = [ receive: { MapClickEvent e ->
				if(e.button == requiredButton)
					mapClickReceiver.receive e.x, e.y
			} ] as InputEventReceiver<MapClickEvent>
		
		final activity = new InputReceivingActivity(this, gameService, MapClickEvent, adapter)
		activities << activity
	}
	
	/**
	 * Register a map-drag-start activity. While the associated tool is active, this activity will
	 * be called whenever the user initiates a drag across the map using the given mouse-button. This activity will
	 * be passed the coordinates of the map-cell that was clicked. (<strong>Note</strong> that the given map-cell may not be a valid cell at all!)
	 * @param button c.f. {@link Input.Buttons}
	 * @param mapStartDragSpec
	 */
	public void mapDragStart(int button, @DelegatesTo(value=MapCoordReceiver, strategy=Closure.DELEGATE_FIRST) Closure mapStartDragSpec) {
		
		mapStartDragSpec.delegate = this
		mapStartDragSpec.resolveStrategy = Closure.DELEGATE_FIRST
		final mapStartDragReceiver = mapStartDragSpec as MapCoordReceiver
		
		final requiredButton = button
		final adapter = [ receive: { MapDragStartEvent e ->
				if(e.button == requiredButton)
					mapStartDragReceiver.receive e.x, e.y
			} ] as InputEventReceiver<MapDragStartEvent>
		final activity = new InputReceivingActivity(this, gameService, MapDragStartEvent, adapter)
		activities << activity
	}
	
	/**
	 * Register a map-drag-update activity. While the associated tool is active, this activity will
	 * be called whenever the user continues a drag across the map using the given mouse-button. This activity will
	 * be passed the coordinates of the map-cell that was clicked. (<strong>Note</strong> that the given map-cell may not be a valid cell at all!)
	 * @param button c.f. {@link Input.Buttons}
	 * @param mapUpdateDragSpec
	 */
	public void mapDragUpdate(int button, @DelegatesTo(value=MapCoordReceiver, strategy=Closure.DELEGATE_FIRST) Closure mapUpdateDragSpec) {
		
		mapUpdateDragSpec.delegate = this
		mapUpdateDragSpec.resolveStrategy = Closure.DELEGATE_FIRST
		final mapUpdateDragReceiver = mapUpdateDragSpec as MapCoordReceiver
		
		final requiredButton = button
		final adapter = [ receive: { MapDragUpdateEvent e ->
				if(e.button == requiredButton)
					mapUpdateDragReceiver.receive e.x, e.y
			} ] as InputEventReceiver<MapDragUpdateEvent>
		final activity = new InputReceivingActivity(this, gameService, MapDragUpdateEvent, adapter)
		activities << activity
	}
	
	/**
	 * Register a map-drag-end activity. While the associated tool is active, this activity will
	 * be called whenever the user concludes a drag across the map using the given mouse-button. This activity will
	 * be passed the coordinates of the map-cell that was clicked. (<strong>Note</strong> that the given map-cell may not be a valid cell at all!)
	 * @param button c.f. {@link Input.Buttons}
	 * @param mapEndDragSpec
	 */
	public void mapDragEnd(int button, @DelegatesTo(value=MapCoordReceiver, strategy=Closure.DELEGATE_FIRST) Closure mapEndDragSpec) {
		
		mapEndDragSpec.delegate = this
		mapEndDragSpec.resolveStrategy = Closure.DELEGATE_FIRST
		final mapEndDragReceiver = mapEndDragSpec as MapCoordReceiver
		
		final requiredButton = button
		final adapter = [ receive: { MapDragEndEvent e ->
				if(e.button == requiredButton)
					mapEndDragReceiver.receive e.x, e.y
			} ] as InputEventReceiver<MapDragEndEvent>
		final activity = new InputReceivingActivity(this, gameService, MapDragEndEvent, adapter)
		activities << activity
	}
	
	@FunctionalInterface
	public interface MapCoordReceiver {
		public void receive(float cellX, float cellY)
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
		activateListeners.each { it.accept thisObject }
		
		try {
			if(enabled)
				activities.each { it.activate() }
		} catch(Throwable t) {
			gameService.state.moduleExceptionRegistry.reportFailure(module, FailureDomain.TOOL_ACTIVATE, t)
			deactivate()
		}
	}
	
	public void update() {
		
		try {
			if(enabled)
				activities.each { it.update() }
		} catch(Throwable t) {
			gameService.state.moduleExceptionRegistry.reportFailure(module, TOOL_UPDATE, t)
			deactivate()
		}
	}
	
	public void deactivate() {
		
		deactivateListeners.each { it.accept thisObject }
		try {
			inactivities.each { it.run() }
			activities.each { it.deactivate() }
		} catch(Throwable t) {
			gameService.state.moduleExceptionRegistry.reportFailure(module, TOOL_DEACTIVATE, t)
		} finally {
			gameService.state.activeTool = null
		}
	}
}
