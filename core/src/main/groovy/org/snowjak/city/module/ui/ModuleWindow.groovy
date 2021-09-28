package org.snowjak.city.module.ui

import java.util.function.BiConsumer

import org.snowjak.city.module.Module

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.utils.Align

/**
 * Provides an interface to a scene2d {@link Window} instance.
 * 
 * @author snowjak88
 *
 */
class ModuleWindow {
	
	private final Window window
	private final Skin skin
	
	final String id
	final Module module
	
	/**
	 * Defines how this window should be aligned within its parent.
	 * @see WindowPin
	 */
	WindowPin pin = WindowPin.NONE
	
	ModuleWindow(String id, Skin skin, Module module) {
		this.id = id
		window = new Window("", skin)
		this.skin = skin
		this.module = module
	}
	
	/**
	 * Set the window's title
	 * @param title
	 */
	public void setTitle(String title) {
		window.titleLabel.text = title
	}
	
	/**
	 * Get the assigned window-title
	 * @return
	 */
	public String getTitle() {
		window.titleLabel.text
	}
	
	/**
	 * Start a new row.
	 * @return the {@link Cell} describing this new row's default attributes
	 */
	public Cell newRow() {
		window.row()
	}
	
	/**
	 * Adds the given text as as {@link Label} to this window. The Label is added to the current row.
	 * @param text
	 * @return the {@link Cell} containing the added Label. Used to configure cell-attributes.
	 */
	public Cell<Label> leftShift(String text) {
		add text
	}
	
	/**
	 * Adds the given text as as {@link Label} to this window. The Label is added to the current row.
	 * @param text
	 * @return the {@link Cell} containing the added Label. Used to configure cell-attributes.
	 */
	public Cell<Label> add(String text) {
		window.add(text)
	}
	
	/**
	 * Adds the given {@link Actor} to this window. This actor is added on the current row.
	 * @param actor
	 * @return the {@link Cell} containing the added Actor. Used to configure cell-attributes.
	 */
	public Cell<Actor> leftShift(Actor actor) {
		add actor
	}
	
	/**
	 * Adds the given {@link Actor} to this window. This actor is added on the current row.
	 * @param actor
	 * @return the {@link Cell} containing the added Actor. Used to configure cell-attributes.
	 */
	public Cell<Actor> add(Actor actor) {
		window.add(actor)
	}
	
	/**
	 * Add the given {@link VisualParameter} to this window. The resulting actor is added on the current row.
	 * @param parameter
	 * @return the {@link Cell} containing the added Actor. Used to configure cell-attributes.
	 */
	public Cell<Actor> leftShift(VisualParameter parameter) {
		add parameter
	}
	
	/**
	 * Add the given {@link VisualParameter} to this window. The resulting actor is added on the current row.
	 * @param parameter
	 * @return the {@link Cell} containing the added Actor. Used to configure cell-attributes.
	 */
	public Cell<Actor> add(VisualParameter parameter) {
		window.add(parameter.type.getActor(skin))
	}
	
	/**
	 * Mark this window as "invisible".
	 */
	public void hide() {
		window.visible = false
	}
	
	/**
	 * Mark this window as "visible".
	 */
	public void show() {
		window.visible = true
	}
	
	/**
	 * Remove all children from this window. Make it "empty".
	 */
	public void clear() {
		window.clear()
		window.pack()
	}
	
	/**
	 * Add this window to the given {@link Stage}.
	 * @param stage
	 */
	public void addTo(Stage stage) {
		stage.addActor window
	}
	
	/**
	 * Remove this window from its current parent (if any).
	 */
	public void removeFromParent() {
		window.remove()
	}
	
	/**
	 * Ensure this window is aligned with the given {@link Rectangle bounds}, consistent with the assigned {@link #pin}.
	 */
	public void realign(Rectangle bounds) {
		if(window.parent && pin)
			pin.align window, bounds
	}
	
	/**
	 * Defines several different places a window may be pinned. Usually, a pinned window is also immoveable.
	 * @author snowjak88
	 *
	 */
	public enum WindowPin {
		/**
		 * No pin. The window is moveable.
		 */
		NONE( true, { w, b -> } ),
		/**
		 * Pinned to the top-left corner of the screen. The window is <em>not</em> moveable.
		 */
		TOP_LEFT( false, { w, b -> w.setPosition((float)b.x, (float)(b.y + b.height), Align.topLeft) } ),
		/**
		 * Pinned to the top-middle of the screen. The window is <em>not</em> moveable.
		 */
		TOP_CENTER( false, { w, b -> w.setPosition((float)(b.x + b.width / 2f), (float)(b.y + b.height), Align.top) } ),
		/**
		 * Pinned to the top-right corner of the screen. The window is <em>not</em> moveable.
		 */
		TOP_RIGHT( false, { w, b -> w.setPosition((float)(b.x + b.width), (float)(b.y + b.height), Align.topRight) } ),
		/**
		 * Pinned to the left-middle of the screen. The window is <em>not</em> moveable.
		 */
		MIDDLE_LEFT( false, { w, b -> w.setPosition((float)b.x, (float)(b.y + b.height / 2f), Align.left) } ),
		/**
		 * Pinned to the center of the screen. The window is <em>not</em> moveable.
		 */
		MIDDLE_CENTER( false, { w, b -> w.setPosition((float)(b.x + b.width / 2f), (float)(b.y + b.height / 2f), Align.center) } ),
		/**
		 * Pinned to the right-middle of the screen. The window is <em>not</em> moveable.
		 */
		MIDDLE_RIGHT( false, { w, b -> w.setPosition((float)(b.x + b.width), (float)(b.y + b.height / 2f), Align.right) } ),
		/**
		 * Pinned to the bottom-left corner of the screen. The window is <em>not</em> moveable.
		 */
		BOTTOM_LEFT( false, { w, b -> w.setPosition((float)b.x, (float)b.y, Align.bottomLeft) } ),
		/**
		 * Pinned to the bottom-middle of the screen. The window is <em>not</em> moveable.
		 */
		BOTTOM_CENTER( false, { w, b -> w.setPosition((float)(b.x + b.width / 2f), (float)b.y, Align.bottom) } ),
		/**
		 * Pinned to the bottom-right corner of the screen. The window is <em>not</em> moveable.
		 */
		BOTTOM_RIGHT( false, { w, b -> w.setPosition((float)(b.x + b.width), (float)b.y, Align.bottomRight) } )
		
		private final BiConsumer<Window,Rectangle> aligner
		final boolean moveable
		
		WindowPin(boolean moveable, BiConsumer<Window,Rectangle> aligner) {
			this.aligner = aligner
		}
		public void align(Window window, Rectangle bounds) {
			aligner.accept window, bounds
		}
	}
}
