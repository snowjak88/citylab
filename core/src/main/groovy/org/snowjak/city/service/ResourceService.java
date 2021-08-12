/**
 * 
 */
package org.snowjak.city.service;

import java.util.Set;

import com.badlogic.gdx.files.FileHandle;

/**
 * Represents a service that allows you to import resources that are implemented
 * as scripts.
 * 
 * @author snowjak88
 *
 */
public interface ResourceService<S, R> {
	
	/**
	 * @return the list of names for successfully-loaded resources
	 */
	public Set<String> getLoadedNames();
	
	/**
	 * Get the {@link FileHandle} associated with the given resource-name (or
	 * {@code null} if no such resource was found).
	 * 
	 * @param name
	 * @return
	 */
	public FileHandle getFile(String name);
	
	/**
	 * Given a script-file with the given {@link FileHandle}, compute the name we
	 * should give the resulting resource.
	 * 
	 * @param file
	 * @return
	 */
	public default String getNameFromFileHandle(FileHandle file) {
		
		return file.nameWithoutExtension();
	}
	
	/**
	 * Get the resource associated with the given name, or {@code null} if no such
	 * resource was loaded.
	 * 
	 * @param name
	 * @return
	 */
	public R get(String name);
	
	/**
	 * Scan the configured directory-structure for script-files.
	 * 
	 * @return
	 */
	public Set<FileHandle> scanDirectoryForScripts();
}
