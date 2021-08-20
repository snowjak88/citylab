/**
 * 
 */
package org.snowjak.city.console.printers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.snowjak.city.console.ui.ConsoleDisplay;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * @author snowjak88
 *
 */
public class IterablePrinter extends AbstractPrinter<Iterable<?>> {
	
	public IterablePrinter(ConsoleDisplay display, Skin skin) {
		
		super(display, skin);
	}
	
	@Override
	public boolean canPrint(Object obj) {
		
		return Iterable.class.isAssignableFrom(obj.getClass());
	}
	
	@Override
	public List<Actor> print(Iterable<?> obj) {
		
		final List<Actor> result = new LinkedList<>();
		
		final Iterator<?> iterator = obj.iterator();
		
		boolean addedFirst = false;
		while (iterator.hasNext()) {
			
			if (addedFirst)
				result.add(getNewLabel(", "));
			
			result.addAll(getDisplay().getPrintFor(iterator.next()));
			addedFirst = true;
		}
		
		return result;
	}
	
}
