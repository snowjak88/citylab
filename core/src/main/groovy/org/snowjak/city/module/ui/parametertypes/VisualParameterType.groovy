package org.snowjak.city.module.ui.parametertypes

import org.snowjak.city.module.ui.VisualParameter

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin

abstract class VisualParameterType {
	
	final VisualParameter parameter
	
	public VisualParameterType(VisualParameter parameter) {
		this.parameter = parameter
	}
	
	/**
	 * Construct the {@link Actor} that actually implements this visual-parameter-type.
	 * 
	 * @param skin
	 * @return
	 */
	public abstract Actor getActor(Skin skin)
}
