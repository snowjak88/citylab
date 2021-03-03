/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleFunctionGradient
import com.sudoplay.joise.module.ModuleFunctionGradient.FunctionGradientAxis

/**
 * @author snowjak88
 *
 */
class ModuleFnGradientConfigurator implements ModuleConfigurator {
	
	public Module source = scalar(0)
	public FunctionGradientAxis axis = FunctionGradientAxis.X_AXIS
	
	@Override
	public Module build() {
		
		def module = new ModuleFunctionGradient()
		module.setSource source
		module.setAxis axis
		module
	}
}