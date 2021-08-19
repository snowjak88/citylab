/**
 * 
 */
package org.snowjak.city.console.printers;

import java.util.LinkedList;
import java.util.List;

import org.snowjak.city.console.ui.ConsoleDisplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;

/**
 * Prints information about a Java type.
 * 
 * @author snowjak88
 *
 */
public class TypePrinter extends AbstractPrinter<Class<?>> {
	
	private static final Color PRIMITIVES = Color.SLATE, CLASSES = Color.BLUE, ENUMS = Color.SCARLET;
	
	public TypePrinter(ConsoleDisplay display, Skin skin) {
		
		super(display, skin);
	}
	
	@Override
	public boolean canPrint(Object obj) {
		
		return (obj instanceof Class);
	}
	
	@Override
	public List<Actor> print(Class<?> obj) {
		
		final Color color;
		
		final Class<?> toExamine;
		if (obj.isArray())
			toExamine = obj.getComponentType();
		else
			toExamine = obj;
		
		if (toExamine.isPrimitive())
			color = PRIMITIVES;
		else if (toExamine.isEnum())
			color = ENUMS;
		else if (toExamine.equals(Void.class))
			color = Color.BLACK;
		else
			color = CLASSES;
		
		final Label simpleNameLabel;
		final List<Actor> result = new LinkedList<>();
		
		simpleNameLabel = getNewLabel(toExamine.getSimpleName(), color);
		
		result.add(simpleNameLabel);
		if (obj.isArray())
			result.add(getNewLabel("[]"));
		
		if (!(obj.isPrimitive() || (obj.isArray() && obj.getComponentType().isArray())))
			simpleNameLabel.addListener(new TextTooltip(toExamine.getName(), getSkin()));
		
		return result;
	}
}
