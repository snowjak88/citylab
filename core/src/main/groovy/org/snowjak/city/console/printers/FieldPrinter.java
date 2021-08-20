/**
 * 
 */
package org.snowjak.city.console.printers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import org.snowjak.city.console.ui.ConsoleDisplay;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Prints information about a Java property -- whether a publicly-accessible
 * property, or a publicly getter/setter combination.
 * 
 * @author snowjak88
 *
 */
public class FieldPrinter extends AbstractPrinter<Field> {
	
	/**
	 * @param display
	 * @param skin
	 */
	public FieldPrinter(ConsoleDisplay display, Skin skin) {
		
		super(display, skin);
	}
	
	@Override
	public boolean canPrint(Object obj) {
		
		if (!(obj instanceof Field))
			return false;
		
		final Field f = (Field) obj;
		
		final boolean isPublic = Modifier.isPublic(f.getModifiers());
		
		final Method getter = getGetter(f), setter = getSetter(f);
		final boolean hasPublicGetterSetter = (getter != null && setter != null)
				&& (Modifier.isPublic(getter.getModifiers()) && Modifier.isPublic(setter.getModifiers()));
		
		return (isPublic || hasPublicGetterSetter);
	}
	
	@Override
	public List<Actor> print(Field obj) {
		
		final LinkedList<Actor> result = new LinkedList<>();
		
		result.addAll(getDisplay().getPrintFor(obj.getType()));
		result.add(getNewLabel(" " + obj.getName()));
		
		return result;
	}
	
	/**
	 * Get a declared method named "get[Fieldname]()", or {@code null} if no such
	 * method exists.
	 * 
	 * @param forField
	 * @return
	 */
	private Method getGetter(Field forField) {
		
		try {
			return forField.getDeclaringClass().getDeclaredMethod("get" + capitalizeInitial(forField.getName()));
		} catch (NoSuchMethodException e) {
			return null;
		}
	}
	
	/**
	 * Get a declared method named "set[Fieldname]()", or {@code null} if no such
	 * method exists.
	 * 
	 * @param forField
	 * @return
	 */
	private Method getSetter(Field forField) {
		
		try {
			return forField.getDeclaringClass().getDeclaredMethod("set" + capitalizeInitial(forField.getName()),
					forField.getType());
		} catch (NoSuchMethodException e) {
			return null;
		}
	}
	
	private String capitalizeInitial(String v) {
		
		if (v == null)
			return null;
		if (v.isEmpty())
			return v;
		
		return Character.toString(Character.toUpperCase(v.charAt(0))).concat(v.substring(1));
	}
}
