/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleTiers

/**
 * @author snowjak88
 *
 */
class ModuleTiersConfigurator implements ModuleConfigurator<ModuleTiers> {
	
	public Module source = scalar(0)
	public int numTiers = 8
	public boolean smooth = false

	@Override
	public ModuleTiers build() {
		def module = new ModuleTiers()
		module.setSource source
		module.setNumTiers numTiers
		module.setSmooth smooth
		module
	}
}