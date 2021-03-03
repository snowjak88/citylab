/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleTranslateDomain

/**
 * @author snowjak88
 *
 */
class ModuleTranslateDomainConfigurator implements ModuleConfigurator<ModuleTranslateDomain> {
	
	public Module source = scalar(0)
	public Module translateX = scalar(0)
	public Module translateY = scalar(0)
	public Module translateZ = scalar(0)
	public Module translateU = scalar(0)
	public Module translateV = scalar(0)
	public Module translateW = scalar(0)
	
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