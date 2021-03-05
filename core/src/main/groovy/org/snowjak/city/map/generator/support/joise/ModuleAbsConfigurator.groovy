/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleAbs

/**
 * @author snowjak88
 *
 */
class ModuleAbsConfigurator implements ModuleConfigurator<ModuleAbs> {
	
	public Module source
	
	ModuleAbsConfigurator() {
		super()
		setSource(0)
	}
	
	void setSource(Module source) {
		
		this.source = source
	}
	
	void setSource(double source) {
		this.source = new ScalarModule(source)
	}
	
	@Override
	public ModuleAbs build() {
		
		def module = new ModuleAbs()
		module.setSource source
	}
}