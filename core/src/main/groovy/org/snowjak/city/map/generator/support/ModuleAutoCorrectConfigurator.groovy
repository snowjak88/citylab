/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleAutoCorrect

/**
 * @author snowjak88
 *
 */
class ModuleAutoCorrectConfigurator implements ModuleConfigurator<ModuleAutoCorrect> {
	
	public Module source = scalar(0)
	public double low = 0
	public double high = 1
	public int samples = 1024
	
	@Override
	public ModuleAutoCorrect build() {
		def module = new ModuleAutoCorrect()
		module.setSource source
		module.setLow low
		module.setHigh high
		module.setSamples samples
		module.calculateAll()
		module
	}
}