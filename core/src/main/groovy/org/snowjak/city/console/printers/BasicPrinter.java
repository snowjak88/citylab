/**
 * 
 */
package org.snowjak.city.console.printers;

import java.util.Arrays;
import java.util.List;

import org.snowjak.city.console.ui.ConsoleDisplay;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Prints {@link CharSequence} objects as simple Strings.
 * 
 * @author snowjak88
 *
 */
public class BasicPrinter extends AbstractPrinter<CharSequence> {
	
	public BasicPrinter(ConsoleDisplay display, Skin skin) {
		
		super(display, skin);
	}
	
	@Override
	public boolean canPrint(Object obj) {
		
		return (obj == null || obj instanceof CharSequence);
	}
	
	@Override
	public List<Actor> print(CharSequence obj) {
		
		final String string;
		if (obj == null)
			string = "null";
		else
			string = obj.toString();
		return Arrays.asList(getNewLabel(string));
	}
}
