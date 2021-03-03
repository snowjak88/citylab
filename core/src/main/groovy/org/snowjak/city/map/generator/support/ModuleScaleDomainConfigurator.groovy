/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleScaleDomain

/**
 * @author snowjak88
 *
 */
class ModuleScaleDomainConfigurator implements ModuleConfigurator<ModuleScaleDomain> {
	
	Module source = scalar(0)
	Module scaleX = scalar(1)
	Module scaleY = scalar(1)
	Module scaleZ = scalar(1)
	Module scaleU = scalar(1)
	Module scaleV = scalar(1)
	Module scaleW = scalar(1)
	
	@Override
	public ModuleScaleDomain build() {
		
		def module = new ModuleScaleDomain()
		module.setSource source
		module.setScaleX scaleX
		module.setScaleY scaleY
		module.setScaleZ scaleZ
		module.setScaleU scaleU
		module.setScaleV scaleV
		module.setScaleW scaleW
		module
	}
}