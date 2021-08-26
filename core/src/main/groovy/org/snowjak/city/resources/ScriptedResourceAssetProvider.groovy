/**
 * 
 */
package org.snowjak.city.resources

import org.snowjak.city.service.GameAssetService

import com.badlogic.gdx.files.FileHandle

/**
 * Provides asset-loading services to {@link ScriptedResource}s.
 * 
 * @author snowjak88
 *
 */
public class ScriptedResourceAssetProvider {
	
	private final ScriptedResource resource
	private final GameAssetService assetService
	
	/**
	 * Indicate that this script depends on the given asset being loaded.
	 * 
	 * @param resource
	 * @param assetService
	 */
	public ScriptedResourceAssetProvider(ScriptedResource resource, GameAssetService assetService) {
		this.resource = resource
		this.assetService = assetService
	}
	
	public void dependsOn(String name, Class<?> type) {
		if(ScriptedResource.isAssignableFrom(type))
			resource.addScriptedDependency(type, name)
		else
			dependsOn resource.scriptDirectory.child(name), type
	}
	
	/**
	 * Indicate that this script depends on the given asset being loaded.
	 * 
	 * @param file
	 * @param type
	 */
	public void dependsOn(FileHandle file, Class<?> type) {
		
		if(ScriptedResource.isAssignableFrom(type))
			resource.addScriptedDependency(type, file.name())
		else {
			resource.addAssetDependency(type, file)
			assetService.load file.path(), type
		}
	}
	
	/**
	 * Get the loaded asset:
	 * <ul>
	 * <li>If {@code type} is a {@link ScriptedResource}, then {@code name} is assumed to be a resource-ID.</li>
	 * <li>Otherwise, {@code name} is assumed to be a file-name, relative to the current script's directory.</li>
	 * </ul>
	 * <p>
	 * If this script is operating in "dependency-detection mode", this method returns {@code null}.
	 * </p>
	 * @param <T>
	 * @param name
	 * @param type
	 * @return
	 */
	public <T> T get(String name, Class<T> type) {
		if(resource.dependencyCheckingMode)
			return null
		
		if(ScriptedResource.isAssignableFrom(type))
			return getByID(name, type)
		
		get resource.scriptDirectory.child(name), type
	}
	
	/**
	 * Get the loaded asset with the given file and type.
	 * <p>
	 * If this script is operating in "dependency-detection mode", this method returns {@code null}.
	 * </p>
	 * @param <T>
	 * @param file
	 * @param type
	 * @return
	 */
	public <T> T get(FileHandle file, Class<T> type) {
		if(resource.dependencyCheckingMode)
			return null
		
		assetService.get file.path(), type
	}
	
	/**
	 * Get the {@link ScriptedResource} denoted by the given {@code id} and {@code type}.
	 * @param <T>
	 * @param id
	 * @param type
	 * @return
	 */
	public <T extends ScriptedResource> T getByID(String id, Class<T> type) {
		if(resource.dependencyCheckingMode)
			return null
		
		assetService.getByID id, type
	}
}
