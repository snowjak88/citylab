package org.snowjak.city.module.ui.parametertypes

import static org.snowjak.city.module.ModuleExceptionRegistry.FailureDomain.VISUAL_PARAMETER

import org.snowjak.city.module.ui.VisualParameter
import org.snowjak.city.util.ui.IntSpinnerField

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent

class IntSpinnerParameter extends VisualParameterType {
	int min, max, step = 1
	
	public IntSpinnerParameter(VisualParameter parameter) {
		
		super(parameter)
	}
	
	@Override
	public Actor getActor(Skin skin) {
		
		final spinner = new IntSpinnerField(skin)
		spinner.min = min
		spinner.max = max
		spinner.step = step
		spinner.value = parameter.value
		
		spinner.addListener([
			changed: { ChangeEvent e, Actor a ->
				final s = (IntSpinnerField) a
				if(parameter.onSet)
					try {
						parameter.onSet.accept(s.value)
					} catch(Throwable t) {
						parameter.module.state.moduleExceptionRegistry.reportFailure(parameter.module, VISUAL_PARAMETER, t)
					}
			}] as ChangeListener)
		
		spinner
	}
}
