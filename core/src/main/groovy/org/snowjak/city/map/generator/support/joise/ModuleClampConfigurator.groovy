/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import java.math.BigDecimal

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleClamp

/**
 * @author snowjak88
 *
 */
class ModuleClampConfigurator implements ModuleConfigurator<ModuleClamp> {
	
	public double low = 0
	public double high = 1
	public Module source
	
	ModuleClampConfigurator() {
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
	public ModuleClamp build() {
		
		def module = new ModuleClamp()
		module.setLow low
		module.setHigh high
		module.setSource source
		module
	}
}