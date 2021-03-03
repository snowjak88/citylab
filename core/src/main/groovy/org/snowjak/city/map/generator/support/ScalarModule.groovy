/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.ModuleInstanceMap
import com.sudoplay.joise.ModuleMap
import com.sudoplay.joise.ModulePropertyMap
import com.sudoplay.joise.module.Module

/**
 * @author snowjak88
 *
 */
class ScalarModule extends Module {

	double value
	
	public ScalarModule(double value) {
		this.value = value
	}
	
	@Override
	public double get(double x, double y) {
		
		value
	}

	@Override
	public double get(double x, double y, double z) {
		
		value
	}

	@Override
	public double get(double x, double y, double z, double w) {
		
		value
	}

	@Override
	public double get(double x, double y, double z, double w, double u, double v) {
		
		value
	}

	@Override
	public void setSeed(String seedName, long seed) {
		
		
	}

	@Override
	public void writeToMap(ModuleMap map) {
		
		
	}

	@Override
	public Module buildFromPropertyMap(ModulePropertyMap props, ModuleInstanceMap map) {
		
		value
	}
}
