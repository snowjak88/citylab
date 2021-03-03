/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.ModuleCellGen
import com.sudoplay.joise.module.ModuleCellular

/**
 * @author snowjak88
 *
 */
class ModuleCellularConfigurator implements ModuleConfigurator<ModuleCellular> {
	
	public long seed = 314159262
	public double[] coefficients = [1, 0, 0, 0]
	
	@Override
	public ModuleCellular build() {
		
		def cellgen = new ModuleCellGen()
		cellgen.setSeed seed
		
		def cellular = new ModuleCellular(cellgen)
		def validCoefficients = (0..3).collect { (coefficients != null && coefficients.length > it) ? coefficients[it] : 0 } as double[]
		cellular.setCoefficients validCoefficients[0], validCoefficients[1], validCoefficients[2], validCoefficients[3]
	}
}