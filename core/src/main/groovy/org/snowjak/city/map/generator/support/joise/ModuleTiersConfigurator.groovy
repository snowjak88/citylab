/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import java.math.BigDecimal

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleTiers

/**
 * @author snowjak88
 *
 */
class ModuleTiersConfigurator implements ModuleConfigurator<ModuleTiers> {
	
	public Module source
	public int numTiers = 8
	public boolean smooth = false
	
	ModuleTiersConfigurator() {
		super()
		setSource(0)
	}
	
	void setSource(double source) {
		this.source = new ScalarModule(source)
	}
	
	void setSource(Module source) {
		this.source = source
	}

	@Override
	public ModuleTiers build() {
		def module = new ModuleTiers()
		module.setSource source
		module.setNumTiers numTiers
		module.setSmooth smooth
		module
	}
}