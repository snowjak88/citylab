/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleInvert

/**
 * @author snowjak88
 *
 */
class ModuleInvertConfigurator implements ModuleConfigurator<ModuleInvert> {
	
	Module source = scalar(0)
	
	@Override
	public ModuleInvert build() {
		
		def module = new ModuleInvert()
		module.setSource source
		module
	}
}