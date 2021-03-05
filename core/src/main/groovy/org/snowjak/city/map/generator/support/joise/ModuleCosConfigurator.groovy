/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import java.math.BigDecimal

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleCos

/**
 * @author snowjak88
 *
 */
class ModuleCosConfigurator implements ModuleConfigurator<ModuleCos> {
	
	public Module source
	
	ModuleCosConfigurator() {
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
	public ModuleCos build() {
		
		def module = new ModuleCos()
		module.setSource source
		module
	}
}