/**
 * 
 */
package org.snowjak.city.console.printers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.snowjak.city.console.ui.ConsoleDisplay;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * Printer for object instances -- excluding:
 * <ul>
 * <li>Primitives</li>
 * <li>Arrays</li>
 * <li>Enums</li>
 * </ul>
 * <p>
 * Produces, for example:
 * 
 * <pre>
 * MyObject {
 *    String aPublicField
 *    int[] anotherPublicField
 *    
 *    int aMethod()
 *    void anotherMethod(Object value)
 * }
 * </pre>
 * </p>
 * 
 * @author snowjak88
 *
 */
public class ObjectPrinter extends AbstractPrinter<Object> {
	
	private Object object;
	
	/**
	 * @param display
	 * @param skin
	 */
	public ObjectPrinter(ConsoleDisplay display, Skin skin) {
		
		super(display, skin);
	}
	
	@Override
	public boolean canPrint(Object obj) {
		
		if (obj == null)
			return false;
		
		return (!(obj instanceof Class || obj instanceof Method || obj instanceof Field) && !(obj instanceof Throwable)
				&& !(obj.getClass().isPrimitive() || obj.getClass().isEnum() || obj.getClass().isArray()));
	}
	
	@Override
	public List<Actor> print(Object obj) {
		
		final List<Actor> result = new LinkedList<>();
		
		final Class<?> clazz = obj.getClass();
		
		Table row = asRow(getDisplay().getPrintFor(clazz));
		row.add(getNewLabel(" {"));
		result.add(row);
		
		final Set<Field> fields = new LinkedHashSet<>();
		for (Field f : clazz.getDeclaredFields())
			fields.add(f);
		for (Field f : clazz.getFields())
			fields.add(f);
		
		boolean addedField = false;
		for (Field f : fields) {
			
			if (!getDisplay().hasPrinterFor(f))
				continue;
			
			row = asRow(getNewLabel("    "));
			getDisplay().getPrintFor(f).forEach(row::add);
			row.add(getNewLabel(": "));
			getDisplay().getPrintFor(getFieldValue(obj, f)).forEach(row::add);
			
			result.add(row);
			addedField = true;
			
		}
		
		if (clazz.getDeclaredMethods().length > 0 && addedField)
			result.add(asRow(getNewLabel(" ")));
		
		final Set<Method> methods = new LinkedHashSet<>();
		for (Method m : clazz.getDeclaredMethods())
			methods.add(m);
		for (Method m : clazz.getMethods())
			methods.add(m);
		
		for (Method m : methods) {
			
			if (!getDisplay().hasPrinterFor(m))
				continue;
			
			row = asRow(getNewLabel("    "));
			getDisplay().getPrintFor(m).forEach(row::add);
			
			result.add(row);
			
		}
		
		result.add(asRow(getNewLabel("}")));
		
		return result;
	}
	
	private Object getFieldValue(Object obj, Field field) {
		
		try {
			if (Modifier.isPublic(field.getModifiers()))
				return field.get(obj);
			
			return getGetter(field).invoke(obj);
			
		} catch (InvocationTargetException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
		
	}
	
	private Method getGetter(Field forField) {
		
		try {
			return forField.getDeclaringClass().getDeclaredMethod("get" + capitalizeInitial(forField.getName()));
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
