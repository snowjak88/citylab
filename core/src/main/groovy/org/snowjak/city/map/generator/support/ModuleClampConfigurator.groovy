/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleClamp

/**
 * @author snowjak88
 *
 */
class ModuleClampConfigurator implements ModuleConfigurator<ModuleClamp> {
	
	public double low = 0
	public double high = 1
	public Module source = scalar(0)
	
	@Override
	public ModuleClamp build() {
		
		def module = new ModuleClamp()
		module.setLow low
		module.setHigh high
		module.setSource source
		module
	}
}