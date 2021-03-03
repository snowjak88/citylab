/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleGain

/**
 * @author snowjak88
 *
 */
class ModuleGainConfigurator implements ModuleConfigurator<ModuleGain> {
	
	public Module source = scalar(0)
	public Module gain = scalar(1)
	
	@Override
	public ModuleGain build() {
		
		def module = new ModuleGain()
		module.setSource source
		module.setGain gain
		module
	}
}