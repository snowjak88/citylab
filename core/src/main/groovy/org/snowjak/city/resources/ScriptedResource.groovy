/**
 * 
 */
package org.snowjak.city.resources

import java.util.function.Consumer

import org.snowjak.city.service.LoggerService

import com.badlogic.gdx.files.FileHandle
import com.github.czyzby.kiwi.log.Logger

/**
 * All resources that are configured by Groovy scripts extend this. This class
 * allows the {@link GameAssetService} to load such resources in order, so that
 * their declared dependencies are honored.
 * <p>
 * Each Resource-script is loaded as a {@link DelegatingScript}. A
 * ScriptedResource implementation is assigned as that script's delegate, then
 * the script is executed. The script populates the delegate's properties and
 * runs its methods, leaving the delegate as a finished resource.
 * </p>
 * <p>
 * A ScriptedResource is executed <strong>twice</strong>:
 * <ol>
 * <li>in "dependency-checking" mode</li>
 * <li>in "normal" mode</li>
 * </ol>
 * Each ScriptedResource must implement its methods such that they check
 * {@link #isDependencyCheckingMode()}. If {@code true}, then the only methods
 * that should continue are those that can help compute this resource's
 * dependencies -- e.g., an "include" method.
 * </p>
 * 
 * @author snowjak88
 *
 */
public abstract class ScriptedResource {
	
	public final Logger log = LoggerService.forClass(this.class)
	
	String id
	boolean dependencyCheckingMode = false
	FileHandle scriptDirectory
	GroovyShell shell
	
	ScriptedResourceAssetProvider assets
	
	final Binding binding = new Binding()
	final Map<String,Set<String>> imports = [:]
	final Map<String,Object> providedObjects = [:]
	private final Map<Class<?>, Set<String>> scriptedDependencies = [:]
	private final Map<FileHandle, Class<?>> assetDependencies = [:]
	
	def propertyMissing(name) {
		binding[name]
	}
	
	def propertyMissing(name, value) {
		binding[name] = value
	}
	
	/**
	 * Indicates that this resource depends on another resource of the same type (given by {@code id}). This resource should not be loaded until the named resource is itself loaded.
	 * @param id
	 */
	public void dependsOn(String id) {
		dependsOn id, this.class
	}
	
	/**
	 * Indicates that this resource depends on something else:
	 * <ul>
	 * <li>A scripted resource, whose ID = {@code name}</li>
	 * <li>Another kind of asset, whose file-name is {@code name} (relative to the current directory)</li>
	 * </ul>
	 */
	public <T extends ScriptedResource> void dependsOn(String name, Class<T> resourceType) {
		
		if(ScriptedResource.isAssignableFrom( resourceType ))
			addScriptedDependency resourceType, name
		else
			addAssetDependency resourceType, name
	}
	
	protected void addScriptedDependency(Class<?> type, String id) {
		
		scriptedDependencies.computeIfAbsent(type, {t -> new HashSet<>()}).add(id)
	}
	
	public Map<Class<?>, Set<String>> getScriptedDependencies() {
		
		Collections.unmodifiableMap(scriptedDependencies)
	}
	
	public void addAssetDependency(Class<?> type, String name) {
		
		addAssetDependency type, scriptDirectory.child(name)
	}
	
	public void addAssetDependency(Class<?> type, FileHandle file) {
		assetDependencies[file] = type
	}
	
	public Map<FileHandle, Class<?>> getAssetDependencies() {
		
		assetDependencies
	}
	
	/**
	 * Indicates that this resource provides an object under the given name to all
	 * subsequently-loaded resources.
	 * <p>
	 * This value will be available to subsequently-loaded resources of the same type,
	 * simply as injected variables.
	 * </p>
	 * <p>
	 * This value will also be available to resources of other types. Supposing that you have
	 * a script for a resource of type {@code MyResource}:
	 * <pre>
	 * [myResource.groovy]
	 *    ...
	 *    id = 'myResource'
	 *    provides myVariable as 'provision'
	 *    ...
	 *
	 * [somewhere else]
	 *    ...
	 *    def r = assets.getByID( 'myResource', MyResourceType )
	 *    def variable = r.provision
	 *    ...
	 * </pre>
	 * In plain-old-Java, you'll want to refer to this resource's {@link Binding}.
	 * </p>
	 * @param value
	 */
	public ProvidesBuilder provides(Object value) {
		new ProvidesBuilder(value)
	}
	
	public class ProvidesBuilder {
		private final Object value
		
		ProvidesBuilder(Object value) {
			this.value = value
		}
		
		public void named(String name) {
			providedObjects["$name"] = value
		}
	}
	
	/**
	 * Get a {@link FileHandle} to a child (file or directory) of the current script's directory.
	 * @param name
	 * @return
	 */
	public FileHandle file(String name) {
		scriptDirectory.child(name)
	}
	
	/**
	 * Include a resource in this definition-script.
	 * <p>
	 * A resource may be one of two types:
	 * <ol>
	 * <li>Another module-definition script</li>
	 * <li>A .JAR file</li>
	 * </ol>
	 * </p>
	 * <h2>Including a Script</h2>
	 * <p>
	 * You can break up your definition among multiple script-files,
	 * allowing for individual files that are smaller and, hopefully, more maintainable.
	 * This script shares its current set of definitions and variables with the included.
	 * All script file-names are relative to the current script's directory:
	 * <pre>
	 * include 'components.groovy'
	 * include 'rules/definitions.groovy'
	 * </pre>
	 * </p>
	 * <h2>Including a JAR</h2>
	 * <p>
	 * If your definition relies on functionality not available from the classes bundled with
	 * the base game, you can include your own JAR as part of your package.
	 * As with script-files, JAR locations are relative to the current script's directory:
	 * <pre>
	 * include 'gdx-ai.1.8.2.jar'
	 * </pre>
	 * </p>
	 *
	 * @param name
	 */
	public void include(String name) {
		final includeHandle = scriptDirectory.child(name)
		
		if(includeHandle.directory)
			throw new RuntimeException("Cannot include resource \"$name\" [${includeHandle.path()}] -- is a directory, not a file.")
		
		if(includeHandle.extension().equalsIgnoreCase("jar"))
			includeJar includeHandle
		else if (includeHandle.extension().equalsIgnoreCase("groovy"))
			includeScript includeHandle
		else
			return
	}
	
	private void includeJar(FileHandle handle) {
		
		final cl = (URLClassLoader) this.class.classLoader
		cl.addURL(handle.file().toURI().toURL())
	}
	
	private void includeScript(FileHandle handle) {
		
		final script = (DelegatingScript) shell.parse(handle.file())
		
		def included = executeInclude handle, { r ->
			r.binding.variables.putAll binding.variables
			r.dependencyCheckingMode = dependencyCheckingMode
			r.providedObjects.putAll providedObjects
			r.scriptDirectory = handle.parent()
			r.shell = shell
			r.assets = new ScriptedResourceAssetProvider(r, assets.assetService)
			
			script.setDelegate r
		}, script
		
		if(dependencyCheckingMode) {
			this.assetDependencies.putAll included.assetDependencies
			included.scriptedDependencies.each { type, ids -> this.scriptedDependencies.computeIfAbsent(type, {t -> new LinkedHashSet<>()}).addAll ids }
			included.imports.each { name ,aliases -> this.imports.computeIfAbsent(name, {n -> new LinkedHashSet<>()} ).addAll aliases }
		} else
			this.providedObjects.putAll included.providedObjects
		
		
		script.getBinding().getVariables().forEach({k, v -> this.getBinding().setVariable((String) k, v)})
	}
	
	/**
	 * Execute an "include". The implementer must:
	 * <ol>
	 * <li>Initialize a new instance of its type.</li>
	 * <li>Execute the given {@code configurer} against the new instance</li>
	 * <li>Execute the script</li>
	 * <li>Incorporate the new instance's values into <strong>this</strong> object's values</li>
	 * </ol>
	 * <p>
	 * For example:
	 * <pre>
	 * protected void executeInclude(FileHandle includeHandle, boolean isDependencyMode, DelegatingScript script) {
	 *     
	 *     final module = new Module(tileSetService)
	 *     configurer.accept module
	 *     
	 *     script.run()
	 *     
	 *     this.systems.putAll module.systems
	 *     this.cellRenderingHooks.addAll module.cellRenderingHooks
	 *     
	 *     this.customRenderingHooks.addAll module.customRenderingHooks
	 *     
	 *     module
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param includeHandle
	 * @param isDependencyMode
	 * @param script
	 * @return the new resource instance after script execution
	 */
	protected abstract ScriptedResource executeInclude(FileHandle includeHandle, Consumer<ScriptedResource> configurer, DelegatingScript script)
}
