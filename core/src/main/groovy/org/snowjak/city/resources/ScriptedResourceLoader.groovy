/**
 * 
 */
package org.snowjak.city.resources

import java.util.Map.Entry

import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.syntax.Types
import org.snowjak.city.service.GameAssetService
import org.snowjak.city.util.Util

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader
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
	
	private final Map<FileHandle, CompilerConfiguration> resourceCompilerConfigs = [:]
	private final Map<FileHandle, R> dependencyChecks = [:]
	private final BiMap<FileHandle,String> filesToIDs = HashBiMap.create()
	
	private final Map<String,Object> providedObjects = [:]
	
	private final Map<FileHandle, R> loaded = [:]
	
	public ScriptedResourceLoader(GameAssetService assetService) {
		super(GameAssetService.FILE_HANDLE_RESOLVER)
		this.assetService = assetService
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
		
		final anyAssetNotLoaded = !r.assetDependencies.isEmpty() && r.assetDependencies.any { assetFile, type ->
			!assetService.isLoaded(assetFile.path(), type)
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
	
	/**
	 * Ensure that the ScriptedResource denoted by {@code file} is completely unregistered from this loader.
	 * @param file
	 */
	public void finishUnloading(FileHandle file) {
		
		final loadedResource = loaded[file]
		if(loadedResource)
			for(def assetDependency : loadedResource.assetDependencies)
				assetService.unload assetDependency.key.path(), assetDependency.value
		
		loaded.remove file
		dependencyChecks.remove file
	}
	
	
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, P parameter) {
		
		final r = dependencyChecks.computeIfAbsent(file, { f ->
			loadResource(f, true)
		})
		
		final dependencies = new Array<AssetDescriptor>()
		r.assetDependencies.forEach({ f,t ->
			dependencies.add(new AssetDescriptor(f,t))
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
			config.scriptBaseClass = AutoProxyingPrintlessDelegatingScript.name
		else
			config.scriptBaseClass = DelegatingScript.name
		
		final shell = new GroovyShell(new GroovyClassLoader(this.class.classLoader), config)
		
		final script = (DelegatingScript) shell.parse(file.file())
		
		final r = newInstance()
		r.dependencyCheckingMode = dependencyMode
		r.setScriptDirectory file.parent()
		r.setScriptFile file
		r.setShell shell
		r.binding.variables.putAll providedObjects
		
		r.setAssets new ScriptedResourceAssetProvider(r, assetService)
		
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
		
		if(!dependencyMode) {
			r.binding.variables.putAll r.providedObjects
			this.providedObjects.putAll r.providedObjects
		}
		
		afterLoad r, assetService, dependencyMode
		
		r
	}
	
	/**
	 * This method is called after this resource is fully loaded (whether in dependency-checking mode or not).
	 * Override this to perform all post-load processing -- e.g., inserting required assets into your resource.
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 * @param resource
	 */
	protected void afterLoad(R resource, GameAssetService assetService, boolean isDependencyMode) {
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
		
		final config = new CompilerConfiguration()
		
		final secureCustomizer = new SecureASTCustomizer()
		secureCustomizer.disallowedTokens = [
			Types.KEYWORD_PACKAGE
		]
		
		final importCustomizer = new ImportCustomizer()
		importCustomizer.addImport "Util", Util.name
		
		config.addCompilationCustomizers secureCustomizer, importCustomizer
		config
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
	
	/**
	 * @return this resource's Class
	 */
	public abstract Class<R> getResourceType()
}
