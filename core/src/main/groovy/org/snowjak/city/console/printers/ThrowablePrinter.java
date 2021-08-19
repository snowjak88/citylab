/**
 * 
 */
package org.snowjak.city.console.printers;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.runtime.StringBufferWriter;
import org.snowjak.city.console.ui.ConsoleDisplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Prints {@link Throwable}s as:
 * 
 * <pre>
 * ...
 * [exception class]: [exception message]
 * [exception stack-trace]
 * ...
 * </pre>
 * 
 * @author snowjak88
 *
 */
public class ThrowablePrinter extends AbstractPrinter<Throwable> {
	
	public ThrowablePrinter(ConsoleDisplay display, Skin skin) {
		
		super(display, skin);
	}
	
	@Override
	public boolean canPrint(Object obj) {
		
		return (obj instanceof Throwable);
	}
	
	@Override
	public List<Actor> print(Throwable obj) {
		
		getDisplay().addConsoleEntry(getNewLabel("[" + obj.getClass().getSimpleName() + "]: " + obj.getMessage()));
		
		final StringBuffer b = new StringBuffer();
		try (PrintWriter bw = new PrintWriter(new StringBufferWriter(b))) {
			obj.printStackTrace(bw);
		}
		return Arrays.asList(getNewLabel(b.toString()));
	}
	
	@Override
	protected Label getNewLabel(String text) {
		
		final Label newLabel = super.getNewLabel(text);
		newLabel.setColor(Color.RED);
		return newLabel;
	}
}
