/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import java.math.BigDecimal

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleInvert

/**
 * @author snowjak88
 *
 */
class ModuleInvertConfigurator implements ModuleConfigurator<ModuleInvert> {
	
	Module source
	
	ModuleInvertConfigurator() {
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
	public ModuleInvert build() {
		
		def module = new ModuleInvert()
		module.setSource source
		module
	}
}