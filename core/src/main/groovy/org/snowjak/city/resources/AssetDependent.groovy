/**
 * 
 */
package org.snowjak.city.resources

import com.badlogic.gdx.files.FileHandle

/**
 * Indicates that a type can report what assets it depends on.
 * @author snowjak88
 *
 */
trait AssetDependent {
	
	private final Map<FileHandle, Class<?>> assetDependencies = [:]
	
	public abstract FileHandle getBaseDirectory()
	
	public void addAssetDependency(Class<?> type, String name) {
		
		addAssetDependency type, baseDirectory.child(name)
	}
	
	public void addAssetDependency(Class<?> type, FileHandle file) {
		assetDependencies[file] = type
	}
	
	public Map<FileHandle, Class<?>> getAssetDependencies() {
		
		Collections.unmodifiableMap(assetDependencies)
	}
}
