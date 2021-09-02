package org.snowjak.city.util;

import java.util.function.Consumer;

/**
 * A {@link Consumer} that accepts 2 {@code int}s.
 * 
 * @author snowjak88
 *
 */
@FunctionalInterface
public interface BiIntConsumer {
	
	public void accept(int u, int v);
}
