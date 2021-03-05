/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import java.math.BigDecimal

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleScaleDomain

/**
 * @author snowjak88
 *
 */
class ModuleScaleDomainConfigurator implements ModuleConfigurator<ModuleScaleDomain> {
	
	Module source
	Module scaleX
	Module scaleY
	Module scaleZ
	Module scaleU
	Module scaleV
	Module scaleW
	
	ModuleScaleDomainConfigurator() {
		super()
		setSource(0)
		setScaleX(0)
		setScaleY(0)
		setScaleZ(0)
		setScaleU(0)
		setScaleV(0)
		setScaleW(0)
	}
	
	void setSource(Module source) {
		this.source = source
	}
	
	void setSource(double source) {
		this.source = new ScalarModule(source)
	}
	
	void setScale(double scale) {
		setScaleX scale
		setScaleY scale
		setScaleZ scale
		setScaleU scale
		setScaleV scale
		setScaleW scale
	}
	
	void setScaleX(Module scaleX) {
		this.scaleX = scaleX
	}
	
	void setScaleX(double scaleX) {
		this.scaleX = new ScalarModule(scaleX)
	}
	
	void setScaleY(Module scaleY) {
		this.scaleY = scaleY
	}
	
	void setScaleY(double scaleY) {
		this.scaleY = new ScalarModule(scaleY)
	}
	
	void setScaleZ(Module scaleZ) {
		this.scaleZ = scaleZ
	}
	
	void setScaleZ(double scaleZ) {
		this.scaleZ = new ScalarModule(scaleZ)
	}
	
	void setScaleU(Module scaleU) {
		this.scaleU = scaleU
	}
	
	void setScaleU(double scaleU) {
		this.scaleU = new ScalarModule(scaleU)
	}
	
	void setScaleV(Module scaleV) {
		this.scaleV = scaleV
	}
	
	void setScaleV(double scaleV) {
		this.scaleV = new ScalarModule(scaleV)
	}
	
	void setScaleW(Module scaleW) {
		this.scaleW = scaleW
	}
	
	void setScaleW(double scaleW) {
		this.scaleW = new ScalarModule(scaleW)
	}
	
	@Override
	public ModuleScaleDomain build() {
		
		def module = new ModuleScaleDomain()
		module.setSource source
		module.setScaleX scaleX
		module.setScaleY scaleY
		module.setScaleZ scaleZ
		module.setScaleU scaleU
		module.setScaleV scaleV
		module.setScaleW scaleW
		module
	}
}