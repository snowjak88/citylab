/**
 * 
 */
package org.snowjak.city.console.printers;

import java.util.List;

import org.snowjak.city.console.ui.ConsoleDisplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * Allows a Console to intelligently print something. A "smart
 * Object.toString()".
 * <p>
 * Takes an object of some type, and transforms it into a sequence of
 * {@link Actor}s.
 * </p>
 * 
 * @author snowjak88
 *
 */
public abstract class AbstractPrinter<T> {
	
	private final ConsoleDisplay display;
	private final Skin skin;
	private final LabelStyle labelStyle, whiteLabelStyle;
	
	public AbstractPrinter(ConsoleDisplay display, Skin skin) {
		
		this.display = display;
		this.skin = skin;
		
		if (skin.has("console", LabelStyle.class))
			labelStyle = skin.get("console", LabelStyle.class);
		else
			labelStyle = skin.get(LabelStyle.class);
		
		if (skin.has("console-white", LabelStyle.class))
			whiteLabelStyle = skin.get("console-white", LabelStyle.class);
		else
			whiteLabelStyle = skin.get(LabelStyle.class);
	}
	
	/**
	 * Returns {@code true} if this ConsolePrinter can handle the given object.
	 * 
	 * @param obj
	 * @return
	 */
	public abstract boolean canPrint(Object obj);
	
	public abstract List<Actor> print(T obj);
	
	/**
	 * @return the owning {@link ConsoleDisplay} instance
	 */
	public ConsoleDisplay getDisplay() {
		
		return display;
	}
	
	public Skin getSkin() {
		
		return skin;
	}
	
	/**
	 * The active Skin's {@code "console"} Label-style, or {@code "default"} as a
	 * fallback.
	 * 
	 * @return
	 */
	protected LabelStyle getLabelStyle() {
		
		return labelStyle;
	}
	
	/**
	 * The active Skin's {@code "console-white"} Label-style (suitable for tinting
	 * via {@link Label#setColor(Color) Label.setColor()}, or {@code "default"} as a
	 * fallback.
	 * 
	 * @return
	 */
	protected LabelStyle getWhiteLabelStyle() {
		
		return whiteLabelStyle;
	}
	
	/**
	 * Get a basic {@link Label} containing the specified text, and outfitted with
	 * the correct style. Does <em>not</em> perform automatic line-wrapping.
	 * 
	 * @param text
	 * @return
	 */
	protected Label getNewLabel(String text) {
		
		final Label newLabel = new Label(text, getLabelStyle());
		newLabel.setWrap(false);
		newLabel.setFontScale(getDisplay().getScale());
		
		return newLabel;
	}
	
	/**
	 * Get a basic {@link Label} containing the specified text, and outfitted with
	 * the correct style. Does <em>not</em> perform automatic line-wrapping.
	 * 
	 * @param text
	 * @param color
	 * @return
	 */
	protected Label getNewLabel(String text, Color color) {
		
		final Label newLabel = new Label(text, getWhiteLabelStyle());
		newLabel.setWrap(false);
		newLabel.setFontScale(getDisplay().getScale());
		newLabel.setColor(color);
		
		return newLabel;
	}
	
	/**
	 * Create a new {@link Table} to package all these {@link Actor}s together as a
	 * single row.
	 * 
	 * @param actors
	 * @return
	 */
	protected Table asRow(List<Actor> actors) {
		
		return asRow(actors.toArray(new Actor[0]));
	}
	
	/**
	 * Create a new {@link Table} to package all these {@link Actor}s together as a
	 * single row.
	 * 
	 * @param actors
	 * @return
	 */
	protected Table asRow(Actor... actors) {
		
		final Table table = new Table(getSkin());
		table.row().left();
		for (Actor actor : actors)
			table.add(actor).fill();
		return table;
	}
}
