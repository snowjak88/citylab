package org.snowjak.city.util.ui

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent
import com.badlogic.gdx.utils.Pools

/**
 * A spinner combines a text-field with +/- buttons.
 * 
 * <p>
 * This component <strong>assumes</strong> that your Skin defines button-styles named "{@code plus}" and "{@code minus}".
 * </p>
 * 
 * @author snowjak88
 *
 */
abstract class SpinnerField<T> extends Table {
	
	boolean disabled = false
	/**
	 * If true, then any typing inside the text-field will
	 * immediately change this spinner's value. If false,
	 * then this spinner's value is updated only when the
	 * text-field loses focus.
	 */
	boolean updateValueInstantly = true
	
	T value = null
	
	private final TextField textField
	private final Button plusButton, minusButton
	
	public SpinnerField(Skin skin) {
		
		textField = new TextField("", skin)
		
		textField.addListener([
			changed: { ChangeEvent e, Actor a ->
				if(disabled)
					return
				final tf = (TextField) a
				if(updateValueInstantly)
					setText tf.text
				e.stop()
			}] as ChangeListener)
		
		textField.addListener([
			keyboardFocusChanged: { FocusEvent e, Actor a, boolean focused ->
				if(disabled)
					return
				final tf = (TextField) a
				if(!focused)
					setText tf.text
			}] as FocusListener)
		
		//
		//
		//
		
		plusButton = new Button(skin, "plus")
		plusButton.programmaticChangeEvents = false
		
		plusButton.addListener([
			changed: {ChangeEvent e, Actor a ->
				if(disabled)
					return
				final b = (Button) a
				if(b.checked) {
					b.checked = false
					plus()
				}
				e.stop()
			}] as ChangeListener)
		
		//
		//
		//
		
		minusButton = new Button(skin, "minus")
		minusButton.programmaticChangeEvents = false
		
		minusButton.addListener([
			changed: {ChangeEvent e, Actor a ->
				if(disabled)
					return
				final b = (Button) a
				if(b.checked) {
					b.checked = false
					minus()
				}
				e.stop()
			}] as ChangeListener)
		
		//
		//
		//
		
		row()
		add(textField).growX()
		add(minusButton)
		add(plusButton)
	}
	
	/**
	 * Get this spinner's value as a String.
	 * @return
	 */
	public String getText() {
		textField.text
	}
	
	/**
	 * Set this spinner's value using the given String. If the given text cannot be
	 * converted to the value-type, then this method has no effect.
	 * 
	 * @param text {@code null} will be ignored
	 * @returns true if this set-event was cancelled for any reason
	 */
	public boolean setText(String text) {
		
		setValue(toValue(text))
	}
	
	/**
	 * Set this spinner's value.
	 * @param value {@code null} will be ignored
	 * @returns true if this set-event was cancelled for any reason
	 */
	public boolean setValue(T value) {
		
		if(value == null)
			return true
		
		if(!isValidValue(value))
			return true
		
		final text = toString(value)
		if(text == null)
			return true
		
		final oldValue = this.value
		this.value = value
		
		final oldText = textField.text
		textField.text = text
		
		final e = Pools.obtain(ChangeEvent.class)
		e.target = this
		e.listenerActor = this
		final cancelled = fire(e)
		if(cancelled) {
			this.value = oldValue
			textField.text = oldText
		}
		Pools.free e
		
		cancelled
	}
	
	/**
	 * Convert the given value to a String.
	 * @param value
	 * @return {@code null} if the given value could not be converted
	 */
	protected abstract String toString(T value);
	
	/**
	 * Convert a String to the value-type.
	 * @param text
	 * @return {@code null} if the given text could not be converted
	 */
	protected abstract T toValue(String text);
	
	protected abstract boolean isValidValue(T value);
	
	/**
	 * Called when the "minus" button is clicked.
	 */
	public abstract void minus();
	
	/**
	 * Called when the "plus" button is clicked.
	 */
	public abstract void plus();
	
	public void setDisabled(boolean disabled) {
		textField.disabled = disabled
		plusButton.disabled = disabled
		minusButton.disabled = disabled
		this.disabled = disabled
	}
}
