package org.snowjak.city.module

import static org.snowjak.city.module.ModuleExceptionRegistry.FailureDomain.OTHER

import java.util.concurrent.Callable
import java.util.function.Consumer

import org.snowjak.city.CityGame
import org.snowjak.city.GameState
import org.snowjak.city.ecs.GatheringEntityListener
import org.snowjak.city.map.renderer.MapMode
import org.snowjak.city.map.renderer.hooks.AbstractRenderingHook
import org.snowjak.city.map.renderer.hooks.DelegatingRenderingHook
import org.snowjak.city.map.renderer.hooks.RenderingHook
import org.snowjak.city.module.ModuleExceptionRegistry.FailureDomain
import org.snowjak.city.module.ui.ModuleWindow
import org.snowjak.city.module.ui.VisualParameter
import org.snowjak.city.resources.ScriptedResource
import org.snowjak.city.service.GameService
import org.snowjak.city.service.I18NService
import org.snowjak.city.service.PreferencesService
import org.snowjak.city.service.SkinService
import org.snowjak.city.service.I18NService.I18NBundleContext
import org.snowjak.city.service.I18NService.ProxiedI18NBundleContext
import org.snowjak.city.service.PreferencesService.ScopedPreferences
import org.snowjak.city.tools.Tool
import org.snowjak.city.tools.ToolGroup
import org.snowjak.city.util.RelativePriority

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.google.common.util.concurrent.ListenableFuture

/**
 * A Module provides game functionality.
 * <p>
 * Technically, a Module defines one or more {@link EntitySystem}s, along with
 * their {@link Component}s. These EntitySystems implement specific aspects of
 * game-functionality.
 * </p>
 * <p>
 * A Module may also define UI elements (buttons, windows, dialogs, ...) and may
 * register input-receivers directly.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class Module extends ScriptedResource {
	
	String title = "(?)", description = "(?)"
	boolean enabled = true
	
	private final PreferencesService preferencesService
	private final SkinService skinService
	private final GameService gameService
	private final I18NService i18nService
	
	/**
	 * The currently-active {@link Skin} that should be applied to all LibGDX Scene2d widgets.
	 */
	@Lazy
	Skin skin = skinService.getCurrent()
	
	/**
	 * This Module's {@link I18NBundleContext}. Allows access to this Module's I18N (internationalization) bundles,
	 * with automatic fallback to the application's default I18N bundles.
	 * <p>
	 * You must configure at least one bundle -- e.g., if your Module folder looks like
	 * <pre>
	 *    myFolder/
	 *        myModule.module.groovy   <-- this file
	 *        i18n/
	 *            myI18n.properties
	 *            myI18n_en.properties
	 *            myI18n_de.properties
	 *            ...
	 * </pre>
	 * You would add your I18N bundle to your module via
	 * <pre>
	 *    i18n.addBundle 'i18n/myI18n'
	 * </pre>
	 * </p>
	 */
	@Lazy
	I18NBundleContext i18n = {
		if(dependencyCheckingMode)
			return new ProxiedI18NBundleContext(scriptDirectory)
		if(!id)
			throw new IllegalStateException("Cannot access [i18n] before setting the Module's [id].")
		i18nService.getContext(id, scriptDirectory)
	}()
	
	/**
	 * A {@link ScopedPreferences} instance. Permits access to the application preferences file.
	 * All preference-references are automatically prefixed with "<code><em>module-ID</em>.</code>" --
	 * for example, {@code preferences.getString('mykey')} in the Module {@code 'myModule'}
	 * translates to a preferences-file lookup of {@code myModule.mykey}.
	 */
	@Lazy
	ScopedPreferences preferences = {
		if(!id)
			throw new IllegalStateException("Cannot access [preferences] before setting the Module's [id].")
		preferencesService.get(id)
	}()
	
	/**
	 * The active {@link GameState}
	 */
	final GameState state
	
	/**
	 * The set of actions to execute when this Module is "activated" when starting a game.
	 */
	final Set<Runnable> onActivationActions = []
	
	/**
	 * The set of actions to execute when this Module is "deactivated" when stopping a game.
	 */
	final Set<Runnable> onDeactivationActions = []
	
	/**
	 * Has this Module been activated?
	 */
	boolean activated = false
	
	/**
	 * Previously-loaded {@link Module}s, by ID.
	 */
	final Map<String,ModulePublicFace> modules = [:]
	
	/**
	 * This Module's {@link EntitySystem}s, by ID
	 */
	final Map<String,EntitySystem> systems = [:]
	
	/**
	 * This Module's defined {@link GatheringEntityListener entity-listeners}
	 */
	final Set<GatheringEntityListener> entityListeners = []
	
	/**
	 * {@link MapMode}s, both those defined in this Module and in previously-loaded Modules
	 */
	final Map<String,MapMode> mapModes = [:]
	
	final Map<String, AbstractRenderingHook> renderingHooks = [:]
	
	/**
	 * This Module's defined {@link ToolGroup}s, by ID
	 */
	final Map<String,ToolGroup> toolGroups = [:]
	
	/**
	 * This Module's defined {@link Tool}s, by ID
	 */
	final Map<String,Tool> tools = [:]
	
	/**
	 * This Module's defined {@link ModuleWindow}s, by ID
	 */
	final Map<String,ModuleWindow> windows = [:]
	
	/**
	 * Set of all this Module's defined {@link VisualParameter}s, by ID
	 */
	final Map<String, VisualParameter> visualParameters = [:]
	
	Module(GameService gameService, PreferencesService preferencesService, SkinService skinService, I18NService i18nService, Binding binding = new Binding()) {
		super(binding)
		this.preferencesService = preferencesService
		this.gameService = gameService
		this.skinService = skinService
		this.i18nService = i18nService
		
		this.state = gameService.state
	}
	
	/**
	 * Add an action to be executed when this Module is activated (when starting a game).
	 * @param action
	 */
	public void onActivate(Runnable action) {
		onActivationActions << action
	}
	
	/**
	 * Add an action to be executed when this Module is deactivated (when stopping a game).
	 * @param action
	 */
	public void onDeactivate(Runnable action) {
		onDeactivationActions << action
	}
	
	/**
	 * Define a new {@link MapMode}, or redefine a previously-defined MapMode.
	 * 
	 * @param id
	 * @param mapModeSpec
	 * @return
	 */
	public MapMode mapMode(String id, @DelegatesTo(value=MapMode,strategy=Closure.DELEGATE_FIRST) Closure mapModeSpec = null) {
		final mm = new MapMode(id)
		if(mapModeSpec) {
			mapModeSpec.delegate = mm
			mapModeSpec.resolveStrategy = Closure.DELEGATE_FIRST
			mapModeSpec()
		}
		mapModes["$mm.id"] = mm
		mm
	}
	
	/**
	 * Specify a RenderHook to be included in the game's render-loop.
	 * <p>
	 * {@code hook} is expected to be of the form:
	 * <pre>
	 * { float delta, Batch batch, ShapeDrawer drawer, RenderingSupport support -> ... }
	 * </pre>
	 * </p>
	 * 
	 * @see {@link com.badlogic.gdx.graphics.g2d.Batch Batch}
	 * @see {@link space.earlygrey.shapedrawer.ShapeDrawer ShapeDrawer}
	 * @see {@link org.snowjak.city.map.renderer.RenderingSupport RenderingSupport}
	 * @param id Identifies this renderer. Subsequent Modules may overwrite this renderer by using the same ID.
	 * @param hook
	 * @return a {@link RelativePriority prioritizer}
	 */
	public RelativePriority renderHook(id, Closure hook) {
		if(isDependencyCheckingMode())
			return new RelativePriority()
		
		hook.resolveStrategy = Closure.DELEGATE_FIRST
		hook.delegate = this
		
		def newHook = new DelegatingRenderingHook(id, state.moduleExceptionRegistry, this, hook as RenderingHook)
		hook.owner = newHook
		renderingHooks["$newHook.id"] = newHook
		newHook.relativePriority
	}
	
	/**
	 * Define a new VisualParameter.
	 */
	public void visualParameter(String id, @DelegatesTo(VisualParameter) Closure parameterSpec) {
		if(isDependencyCheckingMode())
			return
		
		final parameter = new VisualParameter(id, this)
		parameterSpec.delegate = parameter
		parameterSpec.resolveStrategy = Closure.DELEGATE_FIRST
		parameterSpec()
		
		visualParameters["$id"] = parameter
	}
	
	private String legalizeID(String id) {
		id.trim().replaceAll(/[\=?<>,.;:|!@#%\^\&()\[\]{}\-+*\/ ]/, "").replaceFirst(/^[0-9]*/, "")
	}
	
	/**
	 * Create a new {@link IteratingSystem}.
	 * <p>
	 * {@code implementation} is expected to be of the form:
	 * <pre>
	 * { Entity entity, float deltaTime -> ... }
	 * </pre>
	 * </p>
	 * 
	 * @param id
	 * @param family
	 * @param implementation
	 */
	public void iteratingSystem(String id, Family family, Closure implementation) {
		
		if(isDependencyCheckingMode())
			return
		
		//
		// This bit of horror is necessitated because Ashley expects every System that you
		// insert to be a unique type -- and any anonymous inner-classes we create here
		// all appear to have the same type!
		//
		// So we have to ensure that we generate a brand-new Class, with the ID the user specifies.
		//
		final legalID = legalizeID(id)
		final systemClassDefinition = '''
class ''' + legalID + ''' extends com.badlogic.ashley.systems.IteratingSystem {
	final Closure implementation, exceptionReporter
	public ''' + legalID + '''(Family family, Closure implementation, Closure exceptionReporter) {
		super(family);
		this.implementation = implementation
		this.exceptionReporter = exceptionReporter
	}
	
	protected void processEntity(Entity entity, float deltaTime) {
		try {
			implementation(entity, deltaTime)
		} catch(Throwable t) {
			exceptionReporter(t)
			processing = false
		}
	}
}'''
		final systemClass = shell.classLoader.parseClass(systemClassDefinition)
		final system = systemClass.newInstance(family, implementation, {t -> state.moduleExceptionRegistry.reportFailure(this, FailureDomain.ENTITY_SYSTEM, t) })
		
		implementation.owner = system
		implementation.delegate = this
		implementation.resolveStrategy = Closure.DELEGATE_FIRST
		
		systems << ["$id" : system]
	}
	
	/**
	 * Create a new {@link IntervalSystem}.
	 * <p>
	 * {@code implementation} is expected to be of the form:
	 * <pre>
	 * { float deltaTime -> ... }
	 * </pre>
	 * </p>
	 *
	 * @param id
	 * @param interval time between executions, in seconds
	 * @param implementation
	 */
	public void intervalSystem(String id, float interval, Closure implementation) {
		
		if(isDependencyCheckingMode())
			return
		
		final legalID = legalizeID(id)
		final systemClassDefinition = '''
class ''' + legalID + ''' extends com.badlogic.ashley.systems.IntervalSystem {
	final Closure implementation, exceptionReporter
	public ''' + legalID + '''(float interval, Closure implementation, Closure exceptionReporter) {
		super(interval);
		this.implementation = implementation
		this.exceptionReporter = exceptionReporter
	}
	
	protected void updateInterval() {
		try {
			implementation(interval)
		} catch(Throwable t) {
			exceptionReporter(t)
			processing = false
		}
	}
}'''
		final systemClass = shell.classLoader.parseClass(systemClassDefinition)
		final system = systemClass.newInstance(interval, implementation, {t -> state.moduleExceptionRegistry.reportFailure(this, FailureDomain.ENTITY_SYSTEM, t) })
		
		implementation.owner = system
		implementation.delegate = this
		implementation.resolveStrategy = Closure.DELEGATE_FIRST
		
		systems << ["$id" : system]
	}
	
	/**
	 * Create a new {@link IntervalIteratingSystem}.
	 * <p>
	 * {@code implementation} is expected to be of the form:
	 * <pre>
	 * { Entity entity, float deltaTime -> ... }
	 * </pre>
	 * </p>
	 * 
	 * @param id
	 * @param family
	 * @param interval time between executions, in seconds
	 * @param implementation
	 */
	public void intervalIteratingSystem(String id, Family family, float interval, Closure implementation) {
		
		if(isDependencyCheckingMode())
			return
		
		final legalID = legalizeID(id)
		final systemClassDefinition = '''
class ''' + legalID + ''' extends org.snowjak.city.ecs.systems.IntervalIteratingSystem {
	final Closure implementation, exceptionReporter
	public ''' + legalID + '''(Family family, float interval, Closure implementation, Closure exceptionReporter) {
		super(family, interval);
		this.implementation = implementation
		this.exceptionReporter = exceptionReporter
	}
	
	protected void processEntity(Entity entity, float deltaTime) {
		try {
			implementation(entity, deltaTime)
		} catch(Throwable t) {
			exceptionReporter(t)
			processing = false
		}
	}
}'''
		final systemClass = shell.classLoader.parseClass(systemClassDefinition)
		final system = systemClass.newInstance(family, interval, implementation, {t -> state.moduleExceptionRegistry.reportFailure(this, FailureDomain.ENTITY_SYSTEM, t) })
		
		implementation.owner = system
		implementation.delegate = this
		implementation.resolveStrategy = Closure.DELEGATE_FIRST
		
		systems << ["$id" : system]
	}
	
	/**
	 * Create a new {@link WindowIteratingSystem}.
	 * <p>
	 * {@code implementation} is expected to be of the form:
	 * <pre>
	 * { Entity entity, float deltaTime -> ... }
	 * </pre>
	 * </p>
	 *
	 * @param id
	 * @param family
	 * @param window number of entities to process per cycle
	 * @param implementation
	 */
	public void windowIteratingSystem(String id, Family family, int window, Closure implementation) {
		
		if(isDependencyCheckingMode())
			return
		
		final legalID = legalizeID(id)
		final systemClassDefinition = '''
class ''' + legalID + ''' extends org.snowjak.city.ecs.systems.WindowIteratingSystem {
	final Closure implementation, exceptionReporter
	public ''' + legalID + '''(Family family, int window, Closure implementation, Closure exceptionReporter) {
		super(window, family);
		this.implementation = implementation
		this.exceptionReporter = exceptionReporter
	}
	
	protected void processEntity(Entity entity, float deltaTime) {
		try {
			implementation(entity, deltaTime)
		} catch(Throwable t) {
			exceptionReporter(t)
			processing = false
		}
	}
}'''
		final systemClass = shell.classLoader.parseClass(systemClassDefinition)
		final system = systemClass.newInstance(family, window, implementation, {t -> state.moduleExceptionRegistry.reportFailure(this, FailureDomain.ENTITY_SYSTEM, t) })
		
		implementation.owner = system
		implementation.delegate = this
		implementation.resolveStrategy = Closure.DELEGATE_FIRST
		
		systems << ["$id" : system]
	}
	
	/**
	 * Create a new {@link TimeSliceIteratingSystem}.
	 * <p>
	 * {@code implementation} is expected to be of the form:
	 * <pre>
	 * { Entity entity, float deltaTime -> ... }
	 * </pre>
	 * </p>
	 *
	 * @param id
	 * @param family
	 * @param timeSlice maximum time to take each cycle, in seconds
	 * @param implementation
	 */
	public void timeSliceSystem(String id, Family family, float timeSlice, Closure implementation) {
		
		if(isDependencyCheckingMode())
			return
		
		final legalID = legalizeID(id)
		final systemClassDefinition = '''
class ''' + legalID + ''' extends org.snowjak.city.ecs.systems.TimeSliceIteratingSystem {
	final Closure implementation, exceptionReporter
	public ''' + legalID + '''(Family family, float timeSlice, Closure implementation, Closure exceptionReporter) {
		super(family, timeSlice);
		this.implementation = implementation
		this.exceptionReporter = exceptionReporter
	}
	
	protected void processEntity(Entity entity, float deltaTime) {
		try {
			implementation(entity, deltaTime)
		} catch(Throwable t) {
			exceptionReporter(t)
			processing = false
		}
	}
}'''
		final systemClass = shell.classLoader.parseClass(systemClassDefinition)
		final system = systemClass.newInstance(family, timeSlice, implementation, {t -> state.moduleExceptionRegistry.reportFailure(this, FailureDomain.ENTITY_SYSTEM, t) })
		
		implementation.owner = system
		implementation.delegate = this
		implementation.resolveStrategy = Closure.DELEGATE_FIRST
		
		systems << ["$id" : system]
	}
	
	/**
	 * Create a new {@link BulkSystem}.
	 * <p>
	 * {@code implementation} is expected to be of the form:
	 * <pre>
	 * { Set<Entity> entities, float deltaTime -> ... }
	 * </pre>
	 * </p>
	 * @param id
	 * @param family
	 * @param implementation
	 */
	public void bulkSystem(String id, Family family, Closure implementation) {
		
		if(isDependencyCheckingMode())
			return
		
		final legalID = legalizeID(id)
		final systemClassDefinition = '''
class ''' + legalID + ''' extends org.snowjak.city.ecs.systems.BulkSystem {
	final Closure implementation, exceptionReporter
	public ''' + legalID + '''(Family family, Closure implementation, Closure exceptionReporter) {
		super(family);
		this.implementation = implementation
		this.exceptionReporter = exceptionReporter
	}
	
	protected void update(Set<Entity> entities, float deltaTime) {
		try {
			implementation(entities, deltaTime)
		} catch(Throwable t) {
			exceptionReporter(t)
			processing = false
		}
	}
}'''
		final systemClass = shell.classLoader.parseClass(systemClassDefinition)
		final system = systemClass.newInstance(family, implementation, {t -> state.moduleExceptionRegistry.reportFailure(this, FailureDomain.ENTITY_SYSTEM, t) })
		
		implementation.owner = system
		implementation.delegate = this
		implementation.resolveStrategy = Closure.DELEGATE_FIRST
		
		systems << ["$id" : system]
	}
	
	/**
	 * Create a new {@link ListeningSystem}.
	 * <p>
	 * Both {@code added} and {@code dropped} are expected to be of the form:
	 * <pre>
	 * { Entity entity, float deltaTime -> ... }
	 * </pre>
	 * </p>
	 *
	 * @param id
	 * @param family
	 * @param implementation
	 */
	public void listeningSystem(String id, Family family, Closure added, Closure dropped) {
		
		if(isDependencyCheckingMode())
			return
		
		final legalID = legalizeID(id)
		final systemClassDefinition = '''
class ''' + legalID + ''' extends org.snowjak.city.ecs.systems.ListeningSystem {
	final Closure onAdd, onDrop, exceptionReporter
	public ''' + legalID + '''(Family family, Closure onAdd, Closure onDrop, Closure exceptionReporter) {
		super(family);
		this.onAdd = onAdd
		this.onDrop = onDrop
		this.exceptionReporter = exceptionReporter
	}
	
	public void added(Entity entity, float deltaTime) {
		try {
			onAdd(entity, deltaTime)
		} catch(Throwable t) {
			exceptionReporter(t)
			processing = false
		}
	}
	public void dropped(Entity entity, float deltaTime) {
		try {
			onDrop(entity, deltaTime)
		} catch(Throwable t) {
			exceptionReporter(t)
			processing = false
		}
	}
}'''
		final systemClass = shell.classLoader.parseClass(systemClassDefinition)
		final system = systemClass.newInstance(family, added, dropped, {t -> state.moduleExceptionRegistry.reportFailure(this, FailureDomain.ENTITY_SYSTEM, t) })
		
		added.owner = system
		added.delegate = this
		added.resolveStrategy = Closure.DELEGATE_FIRST
		
		dropped.owner = system
		dropped.delegate = this
		dropped.resolveStrategy = Closure.DELEGATE_FIRST
		
		systems << ["$id" : system]
	}
	
	/**
	 * Register the given Component as an "event-Component" -- i.e., a marker-Component that will only persist
	 * on its Entity for a single update-cycle.
	 * <p>
	 * If provided, {@code onEventHandler} will be called whenever an entity is detected holding an instance of this event-Component-type.
	 * {@code onEventHandler} must be of the form:
	 * <pre>
	 * { Entity entity, float deltaTime -> ... }
	 * </pre>
	 * </p>
	 * <p>
	 * Behind the scenes, this entails the creation of a {@link org.snowjak.city.ecs.systems.EventComponentSystem}.
	 * </p>
	 * @param eventType
	 * @param onEventHandler
	 */
	public void eventComponent(Class<Component> eventType, Closure onEventHandler = null) {
		
		if(isDependencyCheckingMode())
			return
		
		final legalID = legalizeID("${id}_${eventType.simpleName}")
		
		final systemClassDefinition = '''
class ''' + legalID + ''' extends org.snowjak.city.ecs.systems.EventComponentSystem {
	private final Closure handler, exceptionReporter
	public ''' + legalID + '''(Class<Component> eventType, Closure onEventHandler, Closure exceptionReporter) {
		super(eventType)
		this.handler = onEventHandler
	}
	
	protected void onEvent(Entity entity, float deltaTime) {
		try {
			handler?.call entity, deltaTime
		} catch(Throwable t) {
			exceptionReporter(t)
			processing = false
		}
	}
}'''
		final systemClass = shell.classLoader.parseClass(systemClassDefinition)
		final system = systemClass.newInstance(eventType, onEventHandler, {t -> state.moduleExceptionRegistry.reportFailure(this, FailureDomain.ENTITY_SYSTEM, t) })
		
		systems << ["$legalID" : system]
	}
	
	/**
	 * Construct a new "Family listener". Unlike a {@link listeningSystem(String, Family, Closure, Closure) listeningSystem},
	 * this does not perform any processing by itself. It merely gathers all Entities matching the given Family
	 * into a single Set, [entities], that can be referenced at any time.
	 * <p>
	 * Behind the scenes, this is implemented by a {@link GatheringEntityListener}.
	 * </p>
	 * 
	 * @param family
	 * @return
	 */
	public GatheringEntityListener familyListener(Family family) {
		final listener = new GatheringEntityListener(family)
		entityListeners << listener
		listener
	}
	
	//
	//
	//
	
	public void buttonGroup(String id, @DelegatesTo(value=ToolGroup, strategy=Closure.DELEGATE_FIRST) Closure groupSpec) {
		
		final group = new ToolGroup(id)
		groupSpec.resolveStrategy = Closure.DELEGATE_FIRST
		groupSpec.delegate = group
		groupSpec()
		
		toolGroups << ["$id" : group]
	}
	
	/**
	 * Register a new {@link Tool} in this module.
	 * @param id
	 * @param toolSpec
	 */
	public void tool(String id, @DelegatesTo(value=Tool, strategy=Closure.DELEGATE_FIRST) Closure toolSpec) {
		
		final tool = new Tool(id, this, scriptDirectory, toolGroups, gameService)
		toolSpec.delegate = tool
		toolSpec.owner = this
		toolSpec.resolveStrategy = Closure.DELEGATE_FIRST
		toolSpec()
		
		if(tool.atlas)
			addAssetDependency TextureAtlas, tool.atlas.path()
		else
			tool.buttons.each { _, button ->
				if(button.buttonUp) addAssetDependency Texture, button.buttonUp
				if(button.buttonDown) addAssetDependency Texture, button.buttonDown
			}
		
		tools << ["$id" : tool]
	}
	
	/**
	 * Submit the given task for background execution. Immediately returns a {@link ListenableFuture}
	 * so you can monitor your task from the main thread and, possibly, retrieve its eventual result.
	 * <p>
	 * Primarily, this delegates to the {@link CityGame#EXECUTOR shared} {@link ListeningExecutorService},
	 * so you could bypass this method and simply submit your task yourself.
	 * </p>
	 * <p>
	 * <em>However</em>, this method automatically wraps your task so as to capture any exceptions
	 * your task might generate, so they can be subsequently presented on the main thread.
	 * </p>
	 * @param task
	 * @return
	 */
	public ListenableFuture<?> submitResultTask(Callable<?> task) {
		return CityGame.EXECUTOR.submit({
			->
			try {
				return task.call()
			} catch(Throwable t) {
				gameService.state.moduleExceptionRegistry.reportFailure this, OTHER, t
				return null
			}
		} as Callable<?>)
	}
	
	/**
	 * Submit the given task for background execution. Immediately returns a {@link ListenableFuture}
	 * so you can monitor your task from the main thread and, possibly, retrieve its eventual result.
	 * <p>
	 * Primarily, this delegates to the {@link CityGame#EXECUTOR shared} {@link ListeningExecutorService},
	 * so you could bypass this method and simply submit your task yourself.
	 * </p>
	 * <p>
	 * <em>However</em>, this method automatically wraps your task so as to capture any exceptions
	 * your task might generate, so they can be subsequently presented on the main thread.
	 * </p>
	 * @param task
	 * @return
	 */
	public ListenableFuture<?> submitTask(Runnable task) {
		return CityGame.EXECUTOR.submit({
			->
			try {
				task.run()
			} catch(Throwable t) {
				gameService.state.moduleExceptionRegistry.reportFailure this, OTHER, t
			}
		} as Runnable)
	}
	
	//
	//
	//
	
	/**
	 * Define a new {@link ModuleWindow} under the given {@code id}.
	 * <p>
	 * The new ModuleWindow is returned. It is also available under <code>windows['<em>id</em>']</code>
	 * </p>
	 * @param id
	 * @param windowSpec
	 * @return the new ModuleWindow, for further manipulation
	 */
	public ModuleWindow window(String id, @DelegatesTo(value=ModuleWindow,strategy=Closure.DELEGATE_FIRST) Closure windowSpec) {
		final newWindow = new ModuleWindow(id, skin, this)
		windowSpec.delegate = newWindow
		windowSpec.resolveStrategy = Closure.DELEGATE_FIRST
		windowSpec()
		
		windows["$id"] = newWindow
		newWindow
	}
	
	@Override
	protected ScriptedResource executeInclude(FileHandle includeHandle, Consumer<ScriptedResource> configurer, DelegatingScript script) {
		
		final module = new Module(gameService, preferencesService, skinService, i18nService, binding)
		configurer.accept module
		
		module.id = this.id
		this.i18n.bundles.each { module.i18n.addBundle it }
		module.modules.putAll this.modules
		
		module.onActivationActions.addAll this.onActivationActions
		module.onDeactivationActions.addAll this.onDeactivationActions
		module.systems.putAll this.systems
		module.entityListeners.addAll this.entityListeners
		module.mapModes.putAll this.mapModes
		module.renderingHooks.putAll this.renderingHooks
		module.toolGroups.putAll this.toolGroups
		module.tools.putAll this.tools
		module.windows.putAll this.windows
		
		script.run()
		
		module.i18n.bundles.each { this.i18n.addBundle it }
		this.onActivationActions.addAll module.onActivationActions
		this.onDeactivationActions.addAll module.onDeactivationActions
		this.systems.putAll module.systems
		this.entityListeners.addAll module.entityListeners
		this.mapModes.putAll module.mapModes
		this.renderingHooks.putAll module.renderingHooks
		this.toolGroups.putAll module.toolGroups
		this.tools.putAll module.tools
		this.windows.putAll module.windows
		
		module
	}
}
