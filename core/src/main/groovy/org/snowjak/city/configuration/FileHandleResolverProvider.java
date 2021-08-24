/**
 * 
 */
package org.snowjak.city.configuration;

import org.apache.groovy.util.Maps;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.github.czyzby.autumn.annotation.Provider;
import com.github.czyzby.autumn.provider.DependencyProvider;

/**
 * Provides the shared {@link MatchingFileHandleResolver} instance.
 * 
 * @author snowjak88
 *
 */
@Provider
public class FileHandleResolverProvider implements DependencyProvider<FileHandleResolver> {
	
	@Override
	public Class<FileHandleResolver> getDependencyType() {
		
		return FileHandleResolver.class;
	}
	
	@Override
	public FileHandleResolver provide() {
		
		return new MatchingFileHandleResolver(Maps.of("^/?data/.*", new LocalFileHandleResolver()),
				new InternalFileHandleResolver());
	}
	
}
