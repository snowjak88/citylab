package org.snowjak.city.util.ui

import com.badlogic.gdx.scenes.scene2d.ui.Skin

/**
 * Derivative of {@link SpinnerField} that operates on integers.
 * @author snowjak88
 *
 */
class IntSpinnerField extends SpinnerField<Integer> {
	int min, max, step = 1
	
	public IntSpinnerField(Skin skin) {
		
		super(skin)
	}
	
	@Override
	protected String toString(Integer value) {
		
		value.toString()
	}
	
	@Override
	protected Integer toValue(String text) {
		
		try {
			return Integer.parseInt(text)
		} catch(NumberFormatException e) {
			return null
		}
	}
	
	@Override
	protected boolean isValidValue(Integer value) {
		
		(value >= min && value <= max)
	}
	
	@Override
	public void minus() {
		
		value -= step
	}
	
	@Override
	public void plus() {
		
		value += step
	}
}
