/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleBias

/**
 * @author snowjak88
 *
 */
class ModuleBiasConfigurator implements ModuleConfigurator<ModuleBias> {
	
	public Module bias = scalar(0)
	public Module source = scalar(0)
	
	@Override
	public ModuleBias build() {
		def module = new ModuleBias()
		module.setBias bias
		module.setSource source
		module
	}
}