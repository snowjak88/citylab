/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleSin

/**
 * @author snowjak88
 *
 */
class ModuleSinConfigurator implements ModuleConfigurator<ModuleSin> {
	
	public Module source = scalar(0)
	
	@Override
	public ModuleSin build() {
		
		def module = new ModuleSin()
		module.setSource source
		module
	}
}