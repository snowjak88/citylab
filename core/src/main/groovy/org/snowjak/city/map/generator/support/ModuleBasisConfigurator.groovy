/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.ModuleBasisFunction
import com.sudoplay.joise.module.ModuleBasisFunction.BasisType
import com.sudoplay.joise.module.ModuleBasisFunction.InterpolationType

/**
 * @author snowjak88
 *
 */
class ModuleBasisConfigurator implements ModuleConfigurator<ModuleBasisFunction> {
	
	public InterpolationType interpolation = InterpolationType.CUBIC
	public BasisType basis = BasisType.SIMPLEX
	public long seed = 314159262
	public double rotationAngle = 0
	
	@Override
	public ModuleBasisFunction build() {
		
		def module = new ModuleBasisFunction()
		module.setInterpolation interpolation
		module.setType basis
		module.setSeed seed
		module.setRotationAngle rotationAngle
		module
	}
}