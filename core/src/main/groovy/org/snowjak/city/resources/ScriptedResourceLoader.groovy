/**
 * 
 */
package org.snowjak.city.resources

import java.util.Map.Entry

import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.snowjak.city.service.GameAssetService

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Array
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

/**
 * @author snowjak88
 *
 */
abstract class ScriptedResourceLoader<R extends ScriptedResource, P extends AssetLoaderParameters<R>> extends AsynchronousAssetLoader<R, P> {
	
	private final GameAssetService assetService
	private final FileHandleResolver resolver
	
	private final Map<FileHandle, CompilerConfiguration> resourceCompilerConfigs = [:]
	private final Map<FileHandle, R> dependencyChecks = [:]
	private final BiMap<FileHandle,String> filesToIDs = HashBiMap.create()
	
	private final Map<String,Object> providedObjects = [:]
	
	private final Map<FileHandle, R> loaded = [:]
	
	public ScriptedResourceLoader(GameAssetService assetService, FileHandleResolver resolver) {
		super(resolver)
		this.assetService = assetService
		this.resolver = resolver
	}
	
	/**
	 * Are this resource's dependencies already loaded?
	 * @param file
	 * @return
	 */
	public boolean isDependenciesSatisfied(FileHandle file) {
		final r = dependencyChecks.computeIfAbsent(file, { f ->
			loadResource(f, true)
		})
		
		final anyAssetNotLoaded = !r.assetDependencies.isEmpty() && r.assetDependencies.any { name, type ->
			!assetService.isLoaded(name, type)
		}
		final anyResourceNotLoaded = !r.scriptedDependencies.isEmpty() && r.scriptedDependencies.any { type, names ->
			names.any { name ->
				!assetService.isLoaded(name, type)
			}
		}
		
		( anyAssetNotLoaded || anyResourceNotLoaded )
	}
	
	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, P parameter) {
		loaded.computeIfAbsent(file, { f ->
			loadResource(f, false)
		})
	}
	
	@Override
	public R loadSync (AssetManager manager, String fileName, FileHandle file, P parameter) {
		loaded.computeIfAbsent(file, { f ->
			loadResource(f, false)
		})
	}
	
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, P parameter) {
		
		final r = dependencyChecks.computeIfAbsent(file, { f ->
			loadResource(f, true)
		})
		
		final dependencies = new Array<AssetDescriptor>()
		r.assetDependencies.forEach({ n,t ->
			dependencies.add(new AssetDescriptor(resolve(n),t))
		})
		dependencies
	}
	
	/**
	 * Get this resource's scripted dependencies. Simply delegates to {@link #getScriptedDependencies(FileHandle)}.
	 * <p>
	 * Given as a Mapping of resource-types to resource-IDs.
	 * </p>
	 * @param fileName
	 * @return
	 */
	public Map<Class<?>, Set<String>> getScriptedDependencies(String fileName) {
		getScriptedDependencies(resolve(fileName))
	}
	
	/**
	 * Get this resource's scripted dependencies.
	 * <p>
	 * Given as a Mapping of resource-types to resource-IDs.
	 * </p>
	 * @param file
	 * @return
	 */
	public Map<Class<?>, Set<String>> getScriptedDependencies(FileHandle file) {
		final r = dependencyChecks.computeIfAbsent(file, { f ->
			loadResource(f, true)
		})
		
		r.scriptedDependencies
	}
	
	/**
	 * Load this resource. If its dependencies are not yet satisfied, returns {@code null}.
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws CompilationFailedException
	 */
	public R get(FileHandle file) throws IOException, CompilationFailedException {
		
		if(!isDependenciesSatisfied(file))
			return null
		
		loaded.computeIfAbsent(file, { f ->
			loadResource(f, false)
		})
	}
	
	public String getResourceID(FileHandle file) {
		final r = dependencyChecks.computeIfAbsent(file, { f ->
			loadResource(f, true)
		})
		r.id
	}
	
	/**
	 * Parse the script given by {@code file}, set its delegate to a
	 * {@link #newDelegateInstance(FileHandle) new delegate instance},
	 * and execute it.
	 * 
	 * @param file
	 * @param dependencyMode
	 * @return
	 * @throws IOException
	 * @throws CompilationFailedException
	 */
	protected R loadResource(FileHandle file, boolean dependencyMode) throws IOException, CompilationFailedException {
		
		final config = resourceCompilerConfigs.computeIfAbsent(file, { f ->
			getDefaultCompilerConfiguration()
		} )
		
		if(dependencyMode)
			config.scriptBaseClass = AutoProxyingDelegatingScript.name
		else
			config.scriptBaseClass = DelegatingScript.name
		
		final shell = new GroovyShell(this.class.classLoader, config)
		
		final script = (DelegatingScript) shell.parse(file.file())
		
		final r = newInstance()
		r.dependencyCheckingMode = dependencyMode
		r.setScriptDirectory file.parent()
		r.setShell shell
		r.binding.variables.putAll providedObjects
		
		script.setDelegate r
		
		script.run()
		
		filesToIDs.put file, r.id
		
		if (!r.imports.isEmpty()) {
			
			final ImportCustomizer importCustomizer = new ImportCustomizer()
			
			for (Entry<String, Set<String>> importDefinition : r.imports.entrySet())
				for (String alias : importDefinition.getValue())
					importCustomizer.addImport(alias, importDefinition.getKey())
			
			addCompilationCustomizer(file, importCustomizer)
		}
		
		if(!dependencyMode)
			this.providedObjects.putAll r.providedObjects
		
		r
	}
	
	/**
	 * Get the default {@link CompilerConfiguration} your resource-scripts should use. This might include --
	 * <ul>
	 * <li>{@link org.codehaus.groovy.control.customizers.ImportCustomizer#addStaticImport(String,String) static imports}</li>
	 * <li>{@link org.codehaus.groovy.control.customizers.SecureASTCustomizer disabling language features}</li>
	 * </ul>
	 * The default implementation returns a normal CompilerConfiguration object.
	 * @return
	 */
	protected CompilerConfiguration getDefaultCompilerConfiguration() {
		
		new CompilerConfiguration()
	}
	
	/**
	 * For the given resource, add a CompilationCustomizer to its default CompilerConfiguration.
	 * @param fileHandle
	 * @param customizer
	 */
	protected void addCompilationCustomizer(FileHandle fileHandle, CompilationCustomizer customizer) {
		resourceCompilerConfigs.computeIfAbsent(fileHandle, { _ ->
			getDefaultCompilerConfiguration()
		}).addCompilationCustomizers(customizer)
	}
	
	/**
	 * Create a new resource instance.
	 * 
	 * @return
	 */
	protected abstract R newInstance()
}
