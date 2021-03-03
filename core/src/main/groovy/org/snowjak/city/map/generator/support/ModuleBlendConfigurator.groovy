/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleBlend

/**
 * @author snowjak88
 *
 */
class ModuleBlendConfigurator implements ModuleConfigurator<ModuleBlend> {
	
	public Module low = scalar(0)
	public Module high = scalar(1)
	public Module control = scalar(0.5)
	
	@Override
	public ModuleBlend build() {
		
		def module = new ModuleBlend()
		module.setLowSource low
		module.setHighSource high
		module.setControlSource control
		module
	}
}