/**
 * 
 */
package org.snowjak.city.console.printers;

import java.util.Arrays;
import java.util.List;

import org.snowjak.city.console.ui.ConsoleDisplay;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
		
		return (obj instanceof CharSequence);
	}
	
	@Override
	public List<Actor> print(CharSequence obj) {
		
		final String string = obj.toString();
		final Label label = getNewLabel(string);
		label.setWrap(true);
		return Arrays.asList(label);
	}
}
