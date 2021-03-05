/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import java.math.BigDecimal

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleCombiner
import com.sudoplay.joise.module.ModuleCombiner.CombinerType

/**
 * @author snowjak88
 *
 */
class ModuleCombinerConfigurator implements ModuleConfigurator<ModuleCombiner> {
	
	public CombinerType type = CombinerType.ADD
	public Module[] sources = []
	
	void setSources(Object[] sources) {
		this.sources = []
		
		for(source in sources)
			if(source instanceof Number)
				sources << new ScalarModule(source.doubleValue())
			else
				sources << source
	}
	
	@Override
	public ModuleCombiner build() {
		
		def module = new ModuleCombiner()
		module.setType type
		sources.eachWithIndex { src, idx -> module.setSource(idx, src) }
		module
	}
}