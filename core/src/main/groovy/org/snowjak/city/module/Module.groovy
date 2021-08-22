package org.snowjak.city.module

import org.snowjak.city.GameData
import org.snowjak.city.configuration.Configuration
import org.snowjak.city.map.renderer.hooks.AbstractCellRenderingHook
import org.snowjak.city.map.renderer.hooks.AbstractCustomRenderingHook
import org.snowjak.city.map.renderer.hooks.CellRenderingHook
import org.snowjak.city.map.renderer.hooks.CustomRenderingHook
import org.snowjak.city.map.renderer.hooks.DelegatingCellRenderingHook
import org.snowjak.city.map.renderer.hooks.DelegatingCustomRenderingHook
import org.snowjak.city.service.TileSetService

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle

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
public class Module {
	
	String id, description
	
	final GameData data = GameData.get()
	final Map<String,EntitySystem> systems = [:]
	final Set<AbstractCellRenderingHook> cellRenderingHooks = []
	final Set<AbstractCustomRenderingHook> customRenderingHooks = []
	final Binding binding = new Binding()
	
	private final TileSetService tileSetService
	private final FileHandle scriptDirectory
	private final GroovyShell shell
	
	boolean dependenciesOnlyMode = false
	
		Module(TileSetService tileSetService, FileHandle scriptDirectory, GroovyShell shell) {
			this.tileSetService = tileSetService
			this.scriptDirectory = scriptDirectory
			this.shell = shell
		}
	
	
	
	def propertyMissing = { name ->
		//
		// Attempt to locate any missing properties in our Binding
		binding[name]
	}
	
	/**
	 * Include a resource in this module-definition.
	 * <p>
	 * A resource may be one of two types:
	 * <ol>
	 * <li>Another module-definition script (that donates its definitions to its includer)</li>
	 * <li>A .JAR file that donates required Java classes</li>
	 * </ol>
	 * </p>
	 * @param name
	 */
	public void include(String name) {
		final includeHandle = scriptDirectory.child(name)
		
		if(!includeHandle.exists())
			throw new FileNotFoundException("Cannot include resource \"$name\" [${includeHandle.path()}] -- does not exist.")
		if(includeHandle.directory)
			throw new RuntimeException("Cannot include resource \"$name\" [${includeHandle.path()}] -- is a directory, not a file.")
		
		if(includeHandle.extension().equalsIgnoreCase("jar"))
			includeJar includeHandle
		else if (includeHandle.extension().equalsIgnoreCase("groovy"))
			includeModuleScript includeHandle
		else
			throw new RuntimeException("Cannot include resource \"$name\" [${includeHandle.path()}] -- neither a '.groovy' nor a '.jar'.")
	}
	
	private void includeJar(FileHandle handle) {
		
		final cl = (URLClassLoader) this.class.classLoader
		cl.addURL(handle.file().toURI().toURL())
	}
	
	private void includeModuleScript(FileHandle handle) {
		
		final script = (DelegatingScript) shell.parse(handle.file())
		
		final Module module = new Module(tileSetService, handle.parent(), shell)
		module.setDependenciesOnlyMode dependenciesOnlyMode
		
		script.setDelegate module
		script.run()
		
		this.systems.putAll module.systems
		this.cellRenderingHooks.addAll module.cellRenderingHooks
		this.customRenderingHooks.addAll module.customRenderingHooks
		
		script.getBinding().getVariables().forEach({k, v -> this.getBinding().setVariable((String) k, v)})
	}
	
	public String preference(String name, String defaultValue = "") {
		Gdx.app.getPreferences(Configuration.PREFERENCES_NAME).getString("$id.$name", defaultValue)
	}
	
	public void cellRenderHook(int priority, CellRenderingHook hook) {
		cellRenderingHooks << new DelegatingCellRenderingHook(priority, hook)
	}
	
	public void renderHook(int priority, CustomRenderingHook hook) {
		customRenderingHooks << new DelegatingCustomRenderingHook(priority, hook)
	}
	
	public void iteratingSystem(String id, Family family, Closure implementation) {
		
		def imp = implementation.rehydrate(this, implementation, implementation)
		imp.resolveStrategy = Closure.DELEGATE_FIRST
		
		def system = new IteratingSystem(family) {
					
					@Override
					protected void processEntity(Entity entity, float deltaTime) {
						
						imp(entity, deltaTime)
					}
				}
		
		systems << ["$id" : system]
	}
}
