/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import java.math.BigDecimal

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleSin

/**
 * @author snowjak88
 *
 */
class ModuleSinConfigurator implements ModuleConfigurator<ModuleSin> {
	
	public Module source
	
	ModuleSinConfigurator() {
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
	public ModuleSin build() {
		
		def module = new ModuleSin()
		module.setSource source
		module
	}
}