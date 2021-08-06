/**
 * 
 */
package org.snowjak.city.input;

import org.snowjak.city.controller.GameScreenController;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;

/**
 * Handles input for the {@link GameScreenController} (excepting those
 * input-events that should be handled by the menu-system).
 * 
 * @author snowjak88
 *
 */
public class GameInputProcessor extends InputAdapter {
	
	final private IntMap<ClickEventReceiver> clickReceivers = new IntMap<>();
	final private IntMap<DragEventReceiver> dragReceivers = new IntMap<>();
	final private IntMap<ScrollEventReceiver> scrollReceivers = new IntMap<>();
	
	private boolean isOngoingDrag = false;
	final private IntIntMap touchStartX = new IntIntMap(), touchStartY = new IntIntMap(), touchButton = new IntIntMap();
	
	/**
	 * Register a new {@link ClickEventReceiver}, returning its
	 * {@link UnregistrationHandle}.
	 * <p>
	 * <strong>Note</strong> that this UnregistrationHandle is <strong>not</strong>
	 * explicitly thread-safe, and should only be called from the main (display)
	 * thread.
	 * </p>
	 * 
	 * @param clickReceiver
	 * @return
	 */
	public UnregistrationHandle register(ClickEventReceiver clickReceiver) {
		
		final int hash = clickReceiver.hashCode();
		clickReceivers.put(hash, clickReceiver);
		return () -> clickReceivers.remove(hash);
	}
	
	/**
	 * Register a new {@link DragEventReceiver}, returning its
	 * {@link UnregistrationHandle}.
	 * <p>
	 * <strong>Note</strong> that this UnregistrationHandle is <strong>not</strong>
	 * explicitly thread-safe, and should only be called from the main (display)
	 * thread.
	 * </p>
	 * 
	 * @param dragReceiver
	 * @return
	 */
	public UnregistrationHandle register(DragEventReceiver dragReceiver) {
		
		final int hash = dragReceiver.hashCode();
		dragReceivers.put(hash, dragReceiver);
		return () -> dragReceivers.remove(hash);
	}
	
	/**
	 * Register a new {@link ScrollEventReceiver}, returning its
	 * {@link UnregistrationHandle}.
	 * <p>
	 * <strong>Note</strong> that this UnregistrationHandle is <strong>not</strong>
	 * explicitly thread-safe, and should only be called from the main (display)
	 * thread.
	 * </p>
	 * 
	 * @param scrollReceiver
	 * @return
	 */
	public UnregistrationHandle register(ScrollEventReceiver scrollReceiver) {
		
		final int hash = scrollReceiver.hashCode();
		scrollReceivers.put(hash, scrollReceiver);
		return () -> scrollReceivers.remove(hash);
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		
		touchStartX.put(pointer, screenX);
		touchStartY.put(pointer, screenY);
		touchButton.put(pointer, button);
		isOngoingDrag = false;
		
		return true;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		
		final int startX = touchStartX.get(pointer, screenX);
		final int startY = touchStartY.get(pointer, screenY);
		
		final boolean isClick = (!isOngoingDrag && startX == screenX && startY == screenY);
		
		if (isClick)
			clickReceivers.values().forEach(r -> r.click(screenX, screenY, button));
		else
			dragReceivers.values().forEach(r -> r.dragEnd(screenX, screenY));
		
		return true;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		
		if (!isOngoingDrag) {
			dragReceivers.values()
					.forEach(r -> r.dragStart(screenX, screenY, touchButton.get(pointer, Input.Buttons.LEFT)));
			isOngoingDrag = true;
		} else
			dragReceivers.values().forEach(r -> r.dragUpdate(screenX, screenY));
		
		return true;
	}
	
	@Override
	public boolean scrolled(float amountX, float amountY) {
		
		scrollReceivers.values().forEach(r -> r.scroll(amountX, amountY));
		
		return true;
	}
	
	/**
	 * Certifies the bearer to unregister something from something else.
	 * 
	 * @author snowjak88
	 *
	 */
	@FunctionalInterface
	public interface UnregistrationHandle {
		
		public void unregisterMe();
	}
	
	public interface Unregisterable {
		
	}
}
