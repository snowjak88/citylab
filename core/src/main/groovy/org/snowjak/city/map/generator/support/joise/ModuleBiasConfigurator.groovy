/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import java.math.BigDecimal

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleBias

/**
 * @author snowjak88
 *
 */
class ModuleBiasConfigurator implements ModuleConfigurator<ModuleBias> {
	
	public Module bias
	public Module source
	
	ModuleBiasConfigurator() {
		super()
		setSource(0)
		setBias(0)
	}
	
	void setBias(double bias) {
		this.bias = new ScalarModule(bias)
	}
	
	
	void setSource(Module source) {
		this.source = source
	}
	
	void setSource(double source) {
		this.source = new ScalarModule(source)
	}
	
	void setBias(Module bias) {
		this.bias = bias
	}
	
	@Override
	public ModuleBias build() {
		def module = new ModuleBias()
		module.setBias bias
		module.setSource source
		module
	}
}