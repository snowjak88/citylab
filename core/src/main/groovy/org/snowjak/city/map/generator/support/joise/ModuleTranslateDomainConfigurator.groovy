/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleTranslateDomain

/**
 * @author snowjak88
 *
 */
class ModuleTranslateDomainConfigurator implements ModuleConfigurator<ModuleTranslateDomain> {
	
	public Module source
	public Module translateX
	public Module translateY
	public Module translateZ
	public Module translateU
	public Module translateV
	public Module translateW
	
	ModuleTranslateDomainConfigurator() {
		super()
		setSource(0)
		setTranslateX(0)
		setTranslateY(0)
		setTranslateZ(0)
		setTranslateU(0)
		setTranslateV(0)
		setTranslateW(0)
	}
	
	void setSource(Module source) {
		this.source = source
	}
	
	void setSource(double source) {
		this.source = new ScalarModule(source)
	}
	
	void setTranslateX(Module translateX) {
		this.translateX = translateX
	}
	
	void setTranslateX(double translateX) {
		this.translateX = new ScalarModule(translateX)
	}
	
	void setTranslateY(Module translateY) {
		this.translateY = translateY
	}
	
	void setTranslateY(double translateY) {
		this.translateY = new ScalarModule(translateY)
	}
	
	void setTranslateZ(Module translateZ) {
		this.translateZ = translateZ
	}
	
	void setTranslateZ(double translateZ) {
		this.translateZ = new ScalarModule(translateZ)
	}
	
	void setTranslateU(Module translateU) {
		this.translateU = translateU
	}
	
	void setTranslateU(double translateU) {
		this.translateU = new ScalarModule(translateU)
	}
	
	void setTranslateV(Module translateV) {
		this.translateV = translateV
	}
	
	void setTranslateV(double translateV) {
		this.translateV = new ScalarModule(translateV)
	}
	
	void setTranslateW(Module translateW) {
		this.translateW = translateW
	}
	
	void setTranslateW(double translateW) {
		this.translateW = new ScalarModule(translateW)
	}
	
	@Override
	public ModuleTranslateDomain build() {
		
		def module = new ModuleTranslateDomain()
		module.setSource source
		module.setTranslateX translateX
		module.setTranslateY translateY
		module.setTranslateZ translateZ
		module.setTranslateU translateU
		module.setTranslateV translateV
		module.setTranslateW translateW
		module
	}
}