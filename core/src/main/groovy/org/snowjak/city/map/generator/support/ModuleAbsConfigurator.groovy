/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleAbs

/**
 * @author snowjak88
 *
 */
class ModuleAbsConfigurator implements ModuleConfigurator<ModuleAbs> {
	
	public Module source = scalar(0)

	@Override
	public ModuleAbs build() {
		
		def module = new ModuleAbs()
		module.setSource source
	}
}