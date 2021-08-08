/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import com.sudoplay.joise.module.Module

/**
 * @author snowjak88
 *
 */
trait ModuleConfigurator<T extends Module> {
	
	abstract T build()
	
}
