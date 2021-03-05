/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleFunctionGradient
import com.sudoplay.joise.module.ModuleFunctionGradient.FunctionGradientAxis

/**
 * @author snowjak88
 *
 */
class ModuleFnGradientConfigurator implements ModuleConfigurator {
	
	public Module source
	public FunctionGradientAxis axis = FunctionGradientAxis.X_AXIS
	
	ModuleFnGradientConfigurator() {
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
	public Module build() {
		
		def module = new ModuleFunctionGradient()
		module.setSource source
		module.setAxis axis
		module
	}
}