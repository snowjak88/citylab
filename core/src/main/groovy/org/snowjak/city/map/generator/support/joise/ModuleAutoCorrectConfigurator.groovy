/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import java.math.BigDecimal

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleAutoCorrect

/**
 * @author snowjak88
 *
 */
class ModuleAutoCorrectConfigurator implements ModuleConfigurator<ModuleAutoCorrect> {
	
	public Module source
	public double low = 0.0
	public double high = 1.0
	public int samples = 1024
	
	ModuleAutoCorrectConfigurator() {
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
	public ModuleAutoCorrect build() {
		def module = new ModuleAutoCorrect()
		module.setSource source
		module.setLow low
		module.setHigh high
		module.setSamples samples
		module.calculateAll()
		module
	}
}