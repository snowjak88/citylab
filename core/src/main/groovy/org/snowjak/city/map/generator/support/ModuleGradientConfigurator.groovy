/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.ModuleGradient

/**
 * @author snowjak88
 *
 */
class ModuleGradientConfigurator implements ModuleConfigurator<ModuleGradient> {
	
	double[] extremes = [
		0,
		1,
		0,
		1,
		0,
		1,
		0,
		1,
		0,
		1,
		0,
		1
	]
	
	@Override
	public ModuleGradient build() {
		
		def module = new ModuleGradient()
		
		if(extremes.length >= 12)
			module.setGradient extremes[0], extremes[1], extremes[2], extremes[3],
					extremes[4], extremes[5], extremes[6], extremes[7],
					extremes[8], extremes[9], extremes[10], extremes[11]
		else if (extremes.length >= 8)
			module.setGradient extremes[0], extremes[1], extremes[2], extremes[3],
					extremes[4], extremes[5], extremes[6], extremes[7]
		else if (extremes.length >= 6)
			module.setGradient extremes[0], extremes[1], extremes[2], extremes[3],
					extremes[4], extremes[5]
		else if (extremes.length >= 4)
			module.setGradient extremes[0], extremes[1], extremes[2], extremes[3]
		
		module
	}
}