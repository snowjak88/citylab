/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleCos

/**
 * @author snowjak88
 *
 */
class ModuleCosConfigurator implements ModuleConfigurator<ModuleCos> {
	
	public Module source = scalar(0)
	
	@Override
	public ModuleCos build() {
		
		def module = new ModuleCos()
		module.setSource source
		module
	}
}