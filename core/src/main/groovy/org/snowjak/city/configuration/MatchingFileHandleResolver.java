/**
 * 
 */
package org.snowjak.city.configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

/**
 * Customized for this application. You may assign multiple
 * {@link FileHandleResolver}s, each with an associated regex-pattern.
 * <p>
 * When a file-name is passed for resolution, each pattern is checked in the
 * order it was registered.
 * </p>
 * <p>
 * For first pattern that matches, the file-name is passed to the associated
 * {@link FileHandleResolver} without modification.
 * </p>
 * <p>
 * If no pattern matches, passes to the configured fallback
 * {@link FileHandleResolver}, or throws a RuntimeException is none is
 * configured.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class MatchingFileHandleResolver implements FileHandleResolver {
	
	private final Map<Pattern, FileHandleResolver> resolvers = new LinkedHashMap<>();
	private FileHandleResolver fallback = null;
	
	public MatchingFileHandleResolver() {
		
		this(null, null);
	}
	
	public MatchingFileHandleResolver(Map<String, FileHandleResolver> resolvers) {
		
		this(resolvers, null);
	}
	
	public MatchingFileHandleResolver(FileHandleResolver fallback) {
		
		this(null, fallback);
	}
	
	public MatchingFileHandleResolver(Map<String, FileHandleResolver> resolvers, FileHandleResolver fallback) {
		
		if (resolvers != null)
			resolvers.forEach((p, r) -> this.resolvers.put(Pattern.compile(p), r));
		
		this.fallback = fallback;
	}
	
	/**
	 * Register a new {@link FileHandleResolver} to be associated with the given
	 * regex-pattern.
	 * 
	 * @see #registerResolver(Pattern, FileHandleResolver)
	 * @param pattern
	 * @param resolver
	 * @throws NullPointerException
	 *             if either {@code pattern} or {@code resolver} are {@code null}
	 */
	public void registerResolver(String pattern, FileHandleResolver resolver) {
		
		registerResolver(Pattern.compile(pattern), resolver);
	}
	
	/**
	 * Register a new {@link FileHandleResolver} to be associated with the given
	 * {@link Pattern}
	 * 
	 * @param pattern
	 * @param resolver
	 * @throws NullPointerException
	 *             if either {@code pattern} or {@code resolver} are {@code null}
	 */
	public void registerResolver(Pattern pattern, FileHandleResolver resolver) {
		
		if (pattern == null || resolver == null)
			throw new NullPointerException();
		
		resolvers.put(pattern, resolver);
	}
	
	public void setFallbackResolver(FileHandleResolver fallback) {
		
		this.fallback = fallback;
	}
	
	@Override
	public FileHandle resolve(String fileName) {
		
		final FileHandleResolver resolver =
		//@formatter:off
				resolvers.entrySet()
						.stream()
						.filter(e -> e.getKey().matcher(fileName).matches())
						.findFirst()
						.map(e -> e.getValue()).orElse(fallback);
		//@formatter:on
		
		if (resolver == null)
			throw new RuntimeException("Cannot pick FileHandleResolver -- no regex-pattern registered matching ["
					+ fileName + "], and no fallback FileHandleResolver set.");
		
		return resolver.resolve(fileName);
	}
}
