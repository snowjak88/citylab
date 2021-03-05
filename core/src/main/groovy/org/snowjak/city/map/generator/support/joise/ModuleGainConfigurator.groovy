/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import java.math.BigDecimal

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleGain

/**
 * @author snowjak88
 *
 */
class ModuleGainConfigurator implements ModuleConfigurator<ModuleGain> {
	
	public Module source
	public Module gain
	
	ModuleGainConfigurator() {
		super()
		setSource(0)
		setGain(0)
	}
	
	
	void setSource(Module source) {
		this.source = source
	}

	void setSource(double source) {
		this.source = new ScalarModule(source)
	}
	
	void setGain(Module gain) {
		this.gain = gain
	}
		
	void setGain(double gain) {
		this.gain = new ScalarModule(gain)
	}
	
	@Override
	public ModuleGain build() {
		
		def module = new ModuleGain()
		module.setSource source
		module.setGain gain
		module
	}
}