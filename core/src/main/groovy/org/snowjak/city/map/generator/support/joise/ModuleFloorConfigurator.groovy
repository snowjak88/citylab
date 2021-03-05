/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import java.math.BigDecimal

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleFloor

/**
 * @author snowjak88
 *
 */
class ModuleFloorConfigurator implements ModuleConfigurator<ModuleFloor> {
	
	public Module source
	
	ModuleFloorConfigurator() {
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
	public ModuleFloor build() {
		
		def module = new ModuleFloor()
		module.setSource source
		module
	}
}