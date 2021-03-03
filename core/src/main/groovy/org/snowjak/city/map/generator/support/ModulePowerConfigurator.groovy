/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModulePow

/**
 * @author snowjak88
 *
 */
class ModulePowerConfigurator implements ModuleConfigurator<ModulePow> {
	
	Module source = scalar(0)
	Module pow = scalar(0)
	
	@Override
	public ModulePow build() {
		
		def module = new ModulePow()
		module.setSource source
		module.setPower pow
		module
	}
}