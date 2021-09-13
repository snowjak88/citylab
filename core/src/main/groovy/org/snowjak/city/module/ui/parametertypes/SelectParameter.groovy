package org.snowjak.city.module.ui.parametertypes

import java.util.function.Function

import org.snowjak.city.module.ModuleExceptionRegistry.FailureDomain
import org.snowjak.city.module.ui.VisualParameter

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.utils.Array

/**
 * Spec for select-box definitions. See {@link VisualParameter#select(Closure) VisualParameter.select()}.
 * @author rr828710
 *
 */
class SelectParameter extends VisualParameterType {
	/**
	 * Values available to the select-box.
	 */
	Closure values
	
	/**
	 * If the values contained in the select-box are not easily convertible to Strings,
	 * this function produces a pretty-String version of any given value.
	 */
	Function<?,String> toString
	
	public SelectParameter(VisualParameter parameter) {
		
		super(parameter);
	}
	
	@Override
	public Actor getActor(Skin skin) {
		
		final myParam = parameter
		final myToString = this.toString
		
		final selectBox = new SelectBox(skin) {
					
					@Override
					protected String toString(Object item) {
						
						if(myToString)
							try {
								return myToString.apply(item)
							} catch(Throwable t) {
								parameter.module.state.moduleExceptionRegistry.reportFailure(parameter.module, FailureDomain.VISUAL_PARAMETER, t)
								return super.toString(item)
							}
						
						return super.toString(item)
					}
				};
		
		try {
			final values = this.values()
			
			if(values.class.isArray())
				selectBox.items = (Object[]) values
			else if(Collection.isAssignableFrom(values.class)) {
				final c = values as Collection
				selectBox.items = c.toArray()
			}
			else if(Array.isAssignableFrom(values.class)) {
				final a = values as Array
				selectBox.items = a.toArray(Object)
			}
			else
				selectBox.setItems values
		} catch(Throwable t) {
			parameter.module.state.moduleExceptionRegistry.reportFailure(parameter.module, FailureDomain.VISUAL_PARAMETER, t)
			
		}
		
		if(parameter.onSet) {
			
			selectBox.addListener( [
				changed: { ChangeEvent event, Actor actor ->
					final s = (SelectBox) actor
					if(s.disabled)
						return
					
					try {
						parameter.onSet.accept s.selected
					} catch(Throwable t) {
						parameter.module.state.moduleExceptionRegistry.reportFailure(parameter.module, FailureDomain.VISUAL_PARAMETER, t)
					}
				}
			] as ChangeListener )
		}
		
		if(parameter.value)
			selectBox.selected = parameter.value
		
		selectBox
	}
}
