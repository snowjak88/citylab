/**
 * 
 */
package org.snowjak.city.map.generator.support

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleSphere

/**
 * @author snowjak88
 *
 */
class ModuleSphereConfigurator implements ModuleConfigurator<ModuleSphere> {
	
	public Module centerX = scalar(0)
	public Module centerY = scalar(0)
	public Module centerZ = scalar(0)
	public Module centerU = scalar(0)
	public Module centerV = scalar(0)
	public Module centerW = scalar(0)
	public Module radius = scalar(0)
	
	@Override
	public ModuleSphere build() {
		
		def module = new ModuleSphere()
		module.setCenterX centerX
		module.setCenterY centerY
		module.setCenterZ centerZ
		module.setCenterU centerU
		module.setCenterV centerV
		module.setCenterW centerW
		module.setRadius radius
		module
	}
}
