/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ScalarParameter

/**
 * @author snowjak88
 *
 */
trait ModuleConfigurator<T extends Module> {
	
	abstract T build()
	
	public Module scalar(double scalar) {
		new ScalarModule(scalar)
	}
}
