/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import java.math.BigDecimal

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ScalarParameter

/**
 * @author snowjak88
 *
 */
trait ModuleConfigurator<T extends Module> {
	
	abstract T build()
	
}
