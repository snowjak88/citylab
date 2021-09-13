package org.snowjak.city.module.ui
import java.util.function.Consumer

import org.snowjak.city.module.Module
import org.snowjak.city.module.ui.parametertypes.IntSpinnerParameter
import org.snowjak.city.module.ui.parametertypes.SelectParameter
import org.snowjak.city.module.ui.parametertypes.VisualParameterType

/**
 * Defines the visual portion of a Module's parameter.
 * @author snowjak88
 *
 */
class VisualParameter {
	
	final Module module
	
	/**
	 * The label to display alongside this parameter.
	 */
	String title
	
	/**
	 * The definition for this parameter's field.
	 */
	VisualParameterType type
	
	/**
	 * This parameter's current value (e.g., loaded from the preferences file)
	 */
	Object value
	
	/**
	 * What action to perform when this parameter is set.
	 */
	Consumer<?> onSet
	
	public VisualParameter(Module module) {
		
		this.module = module
		
		VisualParameter.metaClass.methodMissing = { name, args ->
			getProperty(name).call(*args)
		}
	}
	
	def propertyMissing(name) {
		module.getProperty name
	}
	
	def propertyMissing(name, value) {
		module.setProperty name, value
	}
	
	/**
	 * Configure a select box.
	 * 
	 * @param selectSpec
	 * @return
	 */
	public VisualParameterType select(@DelegatesTo(SelectParameter) Closure selectSpec) {
		final select = new SelectParameter(this)
		
		selectSpec.owner = this
		selectSpec.delegate = select
		selectSpec.resolveStrategy = Closure.DELEGATE_FIRST
		selectSpec()
		
		select
	}
	
	/**
	 * Configure an {@link org.snowjak.city.util.ui.IntSpinnerField IntSpinnerField}.
	 * @param spinnerSpec
	 * @return
	 */
	public VisualParameterType intSpinner(@DelegatesTo(IntSpinnerParameter) Closure spinnerSpec) {
		final spinner = new IntSpinnerParameter(this)
		spinnerSpec.owner = this
		spinnerSpec.delegate = spinner
		spinnerSpec.resolveStrategy = Closure.DELEGATE_FIRST
		spinnerSpec()
		
		spinner
	}
}
