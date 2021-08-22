/**
 * 
 */
package org.snowjak.city.console.printers;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.snowjak.city.console.ui.ConsoleDisplay;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;

/**
 * Prints:
 * <ul>
 * <li>Anything extending {@link CharSequence} as a String wrapped in a plain
 * Actor</li>
 * <li>A primitive as its string equivalent</li>
 * <li>A {@link Collection} or {@link Iterable} as a sequence of printed objects
 * wrapped in "[" and "]"</li>
 * <li>A {@link Map} as a sequence of printed keys and values, separated by ":",
 * and all wrapped in "[" and "]"</li>
 * <li>Anything else as its simple class-name (with its full class-name as a
 * tooltip)</li>
 * </ul>
 * 
 * @author snowjak88
 *
 */
public class BasicPrinter extends AbstractPrinter<Object> {
	
	public BasicPrinter(ConsoleDisplay display, Skin skin) {
		
		super(display, skin);
	}
	
	@Override
	public boolean canPrint(Object obj) {
		
		return true;
	}
	
	@Override
	public List<Actor> print(Object obj) {
		
		if (obj == null) {
			final Label nullLabel = getNewLabel("null");
			nullLabel.setStyle(getItalicLabelStyle());
			return Arrays.asList(nullLabel);
		}
		
		final Class<?> clazz = obj.getClass();
		
		if (CharSequence.class.isAssignableFrom(clazz))
			return Arrays.asList(getNewLabel("\"" + ((CharSequence) obj).toString() + "\""));
		
		if (clazz.isPrimitive()) {
			final String s;
			
			if (clazz.equals(boolean.class))
				s = Boolean.toString((boolean) obj);
			else if (clazz.equals(byte.class))
				s = Byte.toString((byte) obj);
			else if (clazz.equals(char.class))
				s = Character.toString((char) obj);
			else if (clazz.equals(short.class))
				s = Short.toString((short) obj);
			else if (clazz.equals(int.class))
				s = Integer.toString((int) obj);
			else if (clazz.equals(long.class))
				s = Long.toString((long) obj);
			else if (clazz.equals(float.class))
				s = Float.toString((float) obj);
			else
				s = Double.toString((double) obj);
			
			return Arrays.asList(getNewLabel(s));
		}
		
		if (Map.Entry.class.isAssignableFrom(clazz)) {
			final Map.Entry<?, ?> e = (Map.Entry<?, ?>) obj;
			final List<Actor> result = new LinkedList<>();
			result.addAll(getDisplay().getPrintFor(e.getKey()));
			result.add(getNewLabel(":"));
			result.addAll(getDisplay().getPrintFor(e.getValue()));
			return result;
		}
		
		if (Iterable.class.isAssignableFrom(clazz)) {
			@SuppressWarnings("unchecked")
			final Iterable<Object> c = (Iterable<Object>) obj;
			
			final List<Actor> result = new LinkedList<>();
			result.add(getNewLabel("["));
			
			boolean addedFirst = false;
			for (Object v : c) {
				if (addedFirst)
					result.add(getNewLabel(","));
				result.addAll(getDisplay().getPrintFor(v));
			}
			
			result.add(getNewLabel("]"));
			return result;
		}
		
		final Label toStringLabel = getNewLabel(obj.toString());
		toStringLabel.addListener(new TextTooltip(clazz.getName(), getSkin()));
		return Arrays.asList(toStringLabel);
	}
}
