/**
 * 
 */
package org.snowjak.city.map.generator.support

import static com.sudoplay.joise.module.ModuleBasisFunction.BasisType.allSourceBasisTypes

import com.sudoplay.joise.module.ModuleFractal
import com.sudoplay.joise.module.ModuleBasisFunction.BasisType
import com.sudoplay.joise.module.ModuleBasisFunction.InterpolationType
import com.sudoplay.joise.module.ModuleFractal.FractalType

/**
 * @author snowjak88
 *
 */
class ModuleFractalConfigurator implements ModuleConfigurator<ModuleFractal> {
	
	public long seed = 314159262
	public FractalType type = FractalType.HYBRIDMULTI
	public int numOctaves = 5
	public double frequency = 1.0
	public BasisType[] sourceBasisTypes = [
		BasisType.SIMPLEX,
		BasisType.SIMPLEX,
		BasisType.SIMPLEX,
		BasisType.SIMPLEX,
		BasisType.SIMPLEX
	]
	public InterpolationType[] sourceInterpolationTypes = [
		InterpolationType.CUBIC,
		InterpolationType.CUBIC,
		InterpolationType.CUBIC,
		InterpolationType.CUBIC,
		InterpolationType.CUBIC
	]
	
	public void setAllSourceBasisTypes(BasisType type) {
		sourceBasisTypes = (0..numOctaves-1).collect { type } as BasisType[]
	}
	
	public BasisType getAllSourceBasisTypes() {
		( sourceBasisTypes == null || sourceBasisTypes.length < 1 ) null : sourceBasisTypes[0]
	}
	
	public void setAllSourceInterpolationTypes(InterpolationType interpolationType) {
		sourceInterpolationTypes = (0..numOctaves-1).collect { interpolationType } as InterpolationType[]
	}
	
	public InterpolationType getAllSourceInterpolationTypes() {
		( sourceInterpolationTypes == null || sourceInterpolationTypes.length < 1 ) null : sourceInterpolationTypes[0]
	}
	
	@Override
	public ModuleFractal build() {
		
		def module = new ModuleFractal()
		module.setSeed seed
		module.setType type
		module.setNumOctaves numOctaves
		module.setFrequency frequency
		
		if(sourceBasisTypes == null || sourceBasisTypes.length == 0)
			module.setAllSourceBasisTypes allSourceBasisTypes
		else
			(0..numOctaves-1).each { module.getBasis(it).setType ( (sourceBasisTypes.length > it) ? sourceBasisTypes[it] : BasisType.SIMPLEX ) }
		
		if(sourceInterpolationTypes == null || sourceInterpolationTypes.length == 0)
			module.setAllSourceInterpolationTypes allSourceInterpolationTypes
		else
			(0..numOctaves-1).each { module.getBasis(it).setInterpolation ( (sourceInterpolationTypes.length > it) ? sourceInterpolationTypes[it] : InterpolationType.CUBIC ) }
		
		module
	}
}