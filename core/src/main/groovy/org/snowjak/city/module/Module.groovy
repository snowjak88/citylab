package org.snowjak.city.module

import static org.snowjak.city.module.ModuleExceptionRegistry.FailureDomain.OTHER

import java.util.concurrent.Callable
import java.util.function.Consumer

import org.snowjak.city.CityGame
import org.snowjak.city.GameState
import org.snowjak.city.map.renderer.hooks.AbstractCellRenderingHook
import org.snowjak.city.map.renderer.hooks.AbstractCustomRenderingHook
import org.snowjak.city.map.renderer.hooks.CellRenderingHook
import org.snowjak.city.map.renderer.hooks.CustomRenderingHook
import org.snowjak.city.map.renderer.hooks.DelegatingCellRenderingHook
import org.snowjak.city.map.renderer.hooks.DelegatingCustomRenderingHook
import org.snowjak.city.resources.ScriptedResource
import org.snowjak.city.service.GameService
import org.snowjak.city.service.I18NService
import org.snowjak.city.service.PreferencesService
import org.snowjak.city.service.I18NService.I18NBundleContext
import org.snowjak.city.service.I18NService.ProxiedI18NBundleContext
import org.snowjak.city.service.PreferencesService.ScopedPreferences
import org.snowjak.city.tools.Tool
import org.snowjak.city.tools.ToolGroup
import org.snowjak.city.util.RelativePriority

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
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
	
	String description
	
	private final PreferencesService preferencesService
	private final GameService gameService
	private final I18NService i18nService
	
	@Lazy
	I18NBundleContext i18n = {
		if(dependencyCheckingMode)
			return new ProxiedI18NBundleContext(scriptDirectory)
		if(!id)
			throw new IllegalStateException("Cannot access [i18n] before setting the Module's [id].")
		i18nService.getContext(id, scriptDirectory)
	}()
	
	@Lazy
	ScopedPreferences preferences = {
		if(!id)
			throw new IllegalStateException("Cannot access [preferences] before setting the Module's [id].")
		preferencesService.get(id)
	}()
	
	final GameState state
	final Map<String,EntitySystem> systems = [:]
	
	final Set<AbstractCellRenderingHook> cellRenderingHooks = []
	final Set<AbstractCustomRenderingHook> customRenderingHooks = []
	
	final Map<String,ToolGroup> toolGroups = [:]
	final Map<String,Tool> tools = [:]
	
	Module(GameService gameService, PreferencesService preferencesService, I18NService i18nService) {
		super()
		this.preferencesService = preferencesService
		this.gameService = gameService
		this.i18nService = i18nService
		
		this.state = gameService.state
	}
	
	/**
	 * Specify a CellRenderingHook to be included in the game's render-loop.
	 * 
	 * @param id Identifies this cell-renderer. Subsequent Modules may overwrite this renderer by using the same ID.
	 * @param hook
	 * @return a {@link RelativePriority prioritizer}
	 */
	public RelativePriority cellRenderHook(String id, Closure hook) {
		if(isDependencyCheckingMode())
			return new RelativePriority()
		
		hook.resolveStrategy = Closure.DELEGATE_FIRST
		hook.delegate = this
		
		def newHook = new DelegatingCellRenderingHook(id, state.moduleExceptionRegistry, this, hook as CellRenderingHook)
		hook.owner = newHook
		cellRenderingHooks << newHook
		newHook.relativePriority
	}
	
	/**
	 * Specify a CustomRenderHook to be included in the game's render-loop.
	 * 
	 * @param id Identifies this custom-renderer. Subsequent Modules may overwrite this renderer by using the same ID.
	 * @param hook
	 * @return a {@link RelativePriority prioritizer}
	 */
	public RelativePriority customRenderHook(id, Closure hook) {
		if(isDependencyCheckingMode())
			return new RelativePriority()
		
		hook.resolveStrategy = Closure.DELEGATE_FIRST
		hook.delegate = this
		
		def newHook = new DelegatingCustomRenderingHook(id, state.moduleExceptionRegistry, this, hook as CustomRenderingHook)
		hook.owner = newHook
		customRenderingHooks << newHook
		newHook.relativePriority
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
	final Closure implementation
	public ''' + legalID + '''(Family family, Closure implementation) {
		super(family);
		this.implementation = implementation
	}
	
	protected void processEntity(Entity entity, float deltaTime) { implementation(entity, deltaTime) }
}'''
		final systemClass = shell.classLoader.parseClass(systemClassDefinition)
		final system = systemClass.newInstance(family, implementation)
		
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
	final Closure implementation
	public ''' + legalID + '''(float interval, Closure implementation) {
			super(interval);
			this.implementation = implementation
		}
		
		protected void updateInterval() { implementation(interval) }
	}'''
		final systemClass = shell.classLoader.parseClass(systemClassDefinition)
		final system = systemClass.newInstance(interval, implementation)
		
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
	final Closure implementation
	public ''' + legalID + '''(Family family, float interval, Closure implementation) {
			super(family, interval);
			this.implementation = implementation
		}
		
		protected void processEntity(Entity entity, float deltaTime) { implementation(entity, deltaTime) }
	}'''
		final systemClass = shell.classLoader.parseClass(systemClassDefinition)
		final system = systemClass.newInstance(family, interval, implementation)
		
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
	final Closure onAdd, onDrop
	public ''' + legalID + '''(Family family, Closure onAdd, Closure onDrop) {
			super(family);
			this.onAdd = onAdd
			this.onDrop = onDrop
		}
		
		public void added(Entity entity, float deltaTime) { onAdd(entity, deltaTime) }
		public void dropped(Entity entity, float deltaTime) { onDrop(entity, deltaTime) }
	}'''
		final systemClass = shell.classLoader.parseClass(systemClassDefinition)
		final system = systemClass.newInstance(family, added, dropped)
		
		added.owner = system
		added.delegate = this
		added.resolveStrategy = Closure.DELEGATE_FIRST
		
		dropped.owner = system
		dropped.delegate = this
		dropped.resolveStrategy = Closure.DELEGATE_FIRST
		
		systems << ["$id" : system]
	}
	
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
		toolSpec.resolveStrategy = Closure.DELEGATE_FIRST
		toolSpec()
		
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
		return CityGame.EXECUTOR.submit({ ->
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
		return CityGame.EXECUTOR.submit({ ->
			try {
				task.run()
			} catch(Throwable t) {
				gameService.state.moduleExceptionRegistry.reportFailure this, OTHER, t
			}
		} as Runnable)
	}
	
	@Override
	protected ScriptedResource executeInclude(FileHandle includeHandle, Consumer<ScriptedResource> configurer, DelegatingScript script) {
		
		final module = new Module(gameService, preferencesService, i18nService)
		configurer.accept module
		
		module.id = this.id
		this.i18n.bundles.each { module.i18n.addBundle it }
		
		script.run()
		
		module.i18n.bundles.each { this.i18n.addBundle it }
		this.systems.putAll module.systems
		this.cellRenderingHooks.addAll module.cellRenderingHooks
		this.customRenderingHooks.addAll module.customRenderingHooks
		this.toolGroups.putAll module.toolGroups
		this.tools.putAll module.tools
		module.binding.variables.each { n,v -> this.binding[n] = v }
		this.providedObjects.putAll module.providedObjects
		
		module
	}
}
