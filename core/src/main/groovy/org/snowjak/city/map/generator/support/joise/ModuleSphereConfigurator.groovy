/**
 * 
 */
package org.snowjak.city.map.generator.support.joise

import com.sudoplay.joise.module.Module
import com.sudoplay.joise.module.ModuleSphere

/**
 * @author snowjak88
 *
 */
class ModuleSphereConfigurator implements ModuleConfigurator<ModuleSphere> {
	
	public Module centerX
	public Module centerY
	public Module centerZ
	public Module centerU
	public Module centerV
	public Module centerW
	public Module radius
	
	ModuleSphereConfigurator() {
		setCenterX(0)
		setCenterY(0)
		setCenterZ(0)
		setCenterU(0)
		setCenterV(0)
		setCenterW(0)
		setRadius(0)
	}
	
	
	void setCenterX(Module centerX) {
		this.centerX = centerX
	}

	void setCenterX(double centerX) {
		this.centerX = new ScalarModule(centerX)
	}
	
	void setCenterY(Module centerY) {
		this.centerY = centerY
	}

	void setCenterY(double centerY) {
		this.centerY = new ScalarModule(centerY)
	}
	
	void setCenterZ(Module centerZ) {
		this.centerZ = centerZ
	}

	void setCenterZ(double centerZ) {
		this.centerZ = new ScalarModule(centerZ)
	}
	
	void setCenterU(Module centerU) {
		this.centerU = centerU
	}

	void setCenterU(double centerU) {
		this.centerU = new ScalarModule(centerU)
	}
	
	void setCenterV(Module centerV) {
		this.centerV = centerV
	}

	void setCenterV(double centerV) {
		this.centerV = new ScalarModule(centerV)
	}
	
	void setCenterW(Module centerW) {
		this.centerW = centerW
	}
	void setCenterW(double centerW) {
		this.centerW = new ScalarModule(centerW)
	}
	
	void setRadius(Module radius) {
		this.radius = radius
	}
	
	void setRadius(double radius) {
		this.radius = new ScalarModule(radius)
	}
	
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
