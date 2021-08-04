/**
 * 
 */
package org.snowjak.city.map.generator.support;

import java.util.List;

/**
 * @author snowjak88
 *
 */
public interface FlavorsProducer {
	public List<String> get(int x, int y);
}
