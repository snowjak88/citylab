/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleAbs
import com.sudoplay.joise.module.ModuleBasisFunction
import com.sudoplay.joise.module.ModuleCellular
import com.sudoplay.joise.module.ModuleClamp
import com.sudoplay.joise.module.ModuleCos
import com.sudoplay.joise.module.ModuleFloor
import com.sudoplay.joise.module.ModuleFractal
import com.sudoplay.joise.module.ModuleGradient
import com.sudoplay.joise.module.ModuleInvert
import com.sudoplay.joise.module.ModulePow
import com.sudoplay.joise.module.ModuleSin
import com.sudoplay.joise.module.ModuleSphere

/**
 * @author snowjak88
 *
 */
abstract class MapGeneratorConfigurationDsl extends Script {
	
	public Module altitude
	public Closure tiles
	
	public MapGeneratorConfigurationDsl() {
		super()
	}
	
	public Module scalar(double scalar) {
		new ScalarModule(scalar)
	}
	
	/**
	 * Create a ModuleBasisFunction noise module (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * 
	 * @param script
	 * @return
	 */
	public ModuleBasisFunction basis(@DelegatesTo(value=ModuleBasisConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleBasisConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Create a ModuleCellular noise module (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 *
	 * @param script
	 * @return
	 */
	public ModuleCellular cellular(@DelegatesTo(value=ModuleCellularConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleCellularConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Create a ModuleFractal noise module (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public ModuleFractal fractal(@DelegatesTo(value=ModuleFractalConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleFractalConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Create a ModuleGradient noise module (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public ModuleGradient gradient(@DelegatesTo(value=ModuleGradientConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleGradientConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Create a ModuleSphere noise module (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public ModuleSphere sphere(@DelegatesTo(value=ModuleSphereConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleSphereConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Wrap the given Module in an ABS function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param module
	 * @return
	 */
	public Module abs(Module module) {
		def absModule = new ModuleAbs()
		absModule.source = module
		absModule
	}
	
	/**
	 * Wrap the given Module in an ABS function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public Module abs(@DelegatesTo(value=ModuleAbsConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleAbsConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Wrap the given Module in a CLAMP function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param module
	 * @param low
	 * @param hi
	 * @return
	 */
	public Module clamp(Module module, double low, double hi) {
		def clampModule = new ModuleClamp(low, hi)
		clampModule.source = module
		clampModule
	}
	
	/**
	 * Wrap the given Module in an ABS function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public Module clamp(@DelegatesTo(value=ModuleClampConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleClampConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Wrap the given Module in a COSINE function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param module
	 * @return
	 */
	public Module cos(Module module) {
		def cosModule = new ModuleCos()
		cosModule.source = module
		cosModule
	}
	
	/**
	 * Wrap the given Module in a COS function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public Module cos(@DelegatesTo(value=ModuleCosConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleCosConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Wrap the given Module in a SINE function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param module
	 * @return
	 */
	public Module sin(Module module) {
		def sinModule = new ModuleSin()
		sinModule.source = module
		sinModule
	}
	
	/**
	 * Wrap the given Module in a SIN function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public Module sin(@DelegatesTo(value=ModuleSinConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleSinConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Wrap the given Module in a FLOOR function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param module
	 * @return
	 */
	public Module floor(Module module) {
		def floorModule = new ModuleFloor()
		floorModule.source = module
		floorModule
	}
	
	/**
	 * Wrap the given Module in a FLOOR function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public Module floor(@DelegatesTo(value=ModuleFloorConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleFloorConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Wrap the given Module in an INVERT (negate) function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param module
	 * @return
	 */
	public Module invert(Module module) {
		def invertModule = new ModuleInvert()
		invertModule.source = module
		invertModule
	}
	
	/**
	 * Wrap the given Module in an INVERT (negate) function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public Module invert(@DelegatesTo(value=ModuleInvertConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleInvertConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Wrap the given Module in a POWER (exponential) function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param module
	 * @param pow
	 * @return
	 */
	public Module pow(Module module, double pow) {
		def invertModule = new ModulePow()
		invertModule.source = module
		invertModule.power = pow
		invertModule
	}
	
	/**
	 * Wrap the given Module in a POWER (exponential) function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param module
	 * @param pow
	 * @return
	 */
	public Module pow(Module module, Module pow) {
		def invertModule = new ModulePow()
		invertModule.source = module
		invertModule.power = pow
		invertModule
	}
	
	/**
	 * Wrap the given Module in a POWER (exponential) function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public Module pow(@DelegatesTo(value=ModulePowerConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModulePowerConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Wrap the given Module in an Auto-Correct function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public Module autoCorrect(@DelegatesTo(value=ModuleAutoCorrectConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleAutoCorrectConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Wrap the given Module in a Bias function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public Module bias(@DelegatesTo(value=ModuleBiasConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleBiasConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Wrap the given Modules in a Blend function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public Module blend(@DelegatesTo(value=ModuleBlendConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleBlendConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Wrap the given Modules in one of several Combiner functions (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public Module combine(@DelegatesTo(value=ModuleCombinerConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleCombinerConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	/**
	 * Wrap the given Module in a "Gradient" function (see the <a href="https://joise.sudoplaygames.com/modules/">online documentation</a>).
	 * @param script
	 * @return
	 */
	public Module fnGradient(@DelegatesTo(value=ModuleFnGradientConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleFnGradientConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	public Module gain(@DelegatesTo(value=ModuleGainConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleGainConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	public Module translateDomain(@DelegatesTo(value=ModuleTranslateDomainConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleTranslateDomainConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	public Module scaleDomain(@DelegatesTo(value=ModuleScaleDomainConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleScaleDomainConfigurator()
		script.delegate = config
		script()
		config.build()
	}
	
	public Module tiers(@DelegatesTo(value=ModuleTiersConfigurator, strategy=Closure.DELEGATE_FIRST) Closure script) {
		def config = new ModuleTiersConfigurator()
		script.delegate = config
		script()
		config.build()
	}
}