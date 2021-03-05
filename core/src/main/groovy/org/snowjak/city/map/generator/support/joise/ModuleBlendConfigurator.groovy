/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleBlend

/**
 * @author snowjak88
 *
 */
class ModuleBlendConfigurator implements ModuleConfigurator<ModuleBlend> {
	
	public Module low
	public Module high
	public Module control
	
	ModuleBlendConfigurator() {
		super()
		setLow(0)
		setHigh(1)
		setControl(0.5)
	}
		
	void setLow(Module low) {
		this.low = low
	}

	void setLow(double low) {
		this.low = new ScalarModule(low)
	}
	
	void setHigh(Module high) {
		this.high = high
	}
	
	void setHigh(double high) {
		this.high = new ScalarModule(high)
	}
	
	void setControl(Module control) {
		this.control = control
	}
	
	void setControl(double control) {
		this.control = new ScalarModule(control)
	}
	
	@Override
	public ModuleBlend build() {
		
		def module = new ModuleBlend()
		module.setLowSource low
		module.setHighSource high
		module.setControlSource control
		module
	}
}