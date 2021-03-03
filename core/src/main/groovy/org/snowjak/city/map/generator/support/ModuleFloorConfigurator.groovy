/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleFloor

/**
 * @author snowjak88
 *
 */
class ModuleFloorConfigurator implements ModuleConfigurator<ModuleFloor> {
	
	public Module source = scalar(0)
	
	@Override
	public ModuleFloor build() {
		
		def module = new ModuleFloor()
		module.setSource source
		module
	}
}