/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import java.math.BigDecimal

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModulePow

/**
 * @author snowjak88
 *
 */
class ModulePowerConfigurator implements ModuleConfigurator<ModulePow> {
	
	Module source
	Module pow
	
	ModulePowerConfigurator() {
		super()
		setSource(0)
		setPow(0)
	}
	
	
	void setSource(Module source) {
		this.source = source
	}

	void setSource(double source) {
		this.source = new ScalarModule(source)
	}
	
	void setPow(Module pow) {
		this.pow = pow
	}

	void setPow(double pow) {
		this.pow = new ScalarModule(pow)
	}
	
	@Override
	public ModulePow build() {
		
		def module = new ModulePow()
		module.setSource source
		module.setPower pow
		module
	}
}