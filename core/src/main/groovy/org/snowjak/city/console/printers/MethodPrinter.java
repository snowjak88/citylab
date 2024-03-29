/**
 * 
 */
package org.snowjak.city.console.printers;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;

import org.snowjak.city.console.ui.ConsoleDisplay;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Prints information about a Java {@link Method} to the Console -- e.g.,
 * 
 * <pre>
 * ...
 * int compare(Object other)
 * String toString()
 * ...
 * </pre>
 * 
 * Please only try to print {@code public} methods.
 * 
 * @author snowjak88
 *
 */
public class MethodPrinter extends AbstractPrinter<Method> {
	
	public MethodPrinter(ConsoleDisplay display, Skin skin) {
		
		super(display, skin);
	}
	
	@Override
	public boolean canPrint(Object obj) {
		
		if (obj == null)
			return false;
		
		if (!(obj instanceof Method))
			return false;
		
		final Method m = (Method) obj;
		return Modifier.isPublic(m.getModifiers());
	}
	
	@Override
	public List<Actor> print(Method obj) {
		
		final List<Actor> actors = new LinkedList<>();
		
		if (Modifier.isAbstract(obj.getModifiers()))
			actors.add(getNewLabel("abstract "));
		if (Modifier.isStatic(obj.getModifiers()))
			actors.add(getNewLabel("static "));
		if (Modifier.isFinal(obj.getModifiers()))
			actors.add(getNewLabel("final "));
		
		final Actor returnType;
		if (obj.getReturnType().equals(Void.class))
			returnType = getNewLabel("void");
		else
			returnType = getDisplay().getPrintFor(obj.getReturnType()).get(0);
		
		actors.add(returnType);
		
		actors.add(getNewLabel(" " + obj.getName() + "("));
		
		int parameterIndex = 0;
		for (Class<?> p : obj.getParameterTypes()) {
			if (parameterIndex > 0)
				actors.add(getNewLabel(","));
			actors.add(getNewLabel(" "));
			
			actors.add(getDisplay().getPrintFor(p).get(0));
			
			final Parameter param = obj.getParameters()[parameterIndex];
			actors.add(getNewLabel(" " + param.getName()));
		}
		actors.add(getNewLabel(" )"));
		
		return actors;
	}
}
