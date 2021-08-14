/**
 * 
 */
package org.snowjak.city.input;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;

import org.snowjak.city.controller.GameScreenController;
import org.snowjak.city.util.UnregistrationHandle;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntIntMap;

/**
 * Handles input for the {@link GameScreenController} (excepting those
 * input-events that should be handled by the menu-system).
 * 
 * @author snowjak88
 *
 */
public class GameInputProcessor extends InputAdapter {
	
	private final Map<Class<? extends AbstractInputEvent>, Collection<InputEventReceiver>> receiversByParameter = new HashMap<>();
	private final Map<Class<AbstractInputEvent>, Collection<AbstractInputEvent>> eventPool = new HashMap<>();
	
	private boolean isOngoingDrag = false;
	final private IntIntMap touchStartX = new IntIntMap(), touchStartY = new IntIntMap(), touchButton = new IntIntMap();
	
	private final Function<Vector2, Vector2> screenToMapConverter;
	private final Vector2 scratch = new Vector2();
	
	public GameInputProcessor(Function<Vector2, Vector2> screenToMapConverter) {
		
		this.screenToMapConverter = screenToMapConverter;
	}
	
	/**
	 * Register a new {@link InputEventReceiver}, returning its
	 * {@link UnregistrationHandle}.
	 * <p>
	 * If {@code receiver} is {@code null}, returns an UnregistrationHandle that
	 * does nothing.
	 * </p>
	 * 
	 * @param receiver
	 * @return the {@link UnregistrationHandle} for the registered receiver
	 */
	public <E extends AbstractInputEvent> UnregistrationHandle register(final Class<E> eventType,
			final InputEventReceiver<E> receiver) {
		
		if (receiver == null)
			return () -> {};
		
		synchronized (receiversByParameter) {
			receiversByParameter.computeIfAbsent(eventType, (t) -> new LinkedHashSet<>()).add(receiver);
			
			return () -> {
				synchronized (receiversByParameter) {
					receiversByParameter.get(eventType).remove(receiver);
				}
			};
		}
	}
	
	protected <E extends AbstractInputEvent> boolean hasReceiversFor(Class<E> eventType) {
		
		synchronized (receiversByParameter) {
			return receiversByParameter.containsKey(eventType) && receiversByParameter.get(eventType) != null;
		}
	}
	
	/**
	 * Dispatch the given {@link AbstractInputEvent} to all registered
	 * {@link InputEventReceiver}s.
	 * <p>
	 * This method will call {@link #retireEventInstance(AbstractInputEvent)
	 * retureEventInstance()} once it's done. Don't plan on re-using the
	 * event-instance you send here.
	 * </p>
	 * 
	 * @param <E>
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	protected <E extends AbstractInputEvent> void sendEvent(E event) {
		
		synchronized (receiversByParameter) {
			if (!hasReceiversFor(event.getClass()))
				//
				// No registered receivers for this event-type.
				return;
			
			for (InputEventReceiver<E> receiver : receiversByParameter.get(event.getClass()))
				receiver.receive(event);
			
			retireEventInstance(event);
		}
	}
	
	/**
	 * Attempt to get an event-instance of the given type from the internal pool. If
	 * no such instance exists yet, this method will attempt to create a new
	 * instance using the default constructor.
	 * 
	 * @param <E>
	 * @param eventType
	 * @return
	 */
	protected <E extends AbstractInputEvent> E getEventInstance(Class<E> eventType) {
		
		synchronized (eventPool) {
			final LinkedList<E> pool = getEventPool(eventType);
			
			if (!pool.isEmpty())
				return pool.peekFirst();
			
			try {
				return eventType.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(
						"Could not instantiate a new input-event of type [" + eventType.getName() + "].", e);
			}
		}
	}
	
	/**
	 * Retire an event-instance to the internal pool.
	 * 
	 * @param <E>
	 * @param event
	 */
	protected <E extends AbstractInputEvent> void retireEventInstance(E event) {
		
		synchronized (eventPool) {
			@SuppressWarnings("unchecked")
			final LinkedList<E> pool = (LinkedList<E>) getEventPool(event.getClass());
			event.reset();
			pool.addLast(event);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <E extends AbstractInputEvent> LinkedList<E> getEventPool(Class<E> eventType) {
		
		synchronized (eventPool) {
			return (LinkedList<E>) eventPool.computeIfAbsent((Class<AbstractInputEvent>) eventType,
					(t) -> new LinkedList<AbstractInputEvent>());
		}
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		
		if (hasReceiversFor(ScreenHoverEvent.class)) {
			final ScreenHoverEvent e = getEventInstance(ScreenHoverEvent.class);
			e.setX(screenX);
			e.setY(screenY);
			sendEvent(e);
		}
		
		if (hasReceiversFor(MapHoverEvent.class)) {
			final MapHoverEvent e = getEventInstance(MapHoverEvent.class);
			scratch.set(screenX, screenY);
			final Vector2 result = screenToMapConverter.apply(scratch);
			e.setX((int) result.x);
			e.setY((int) result.y);
			sendEvent(e);
		}
		
		return true;
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
		
		if (isClick) {
			//
			// Handle click-events.
			//
			if (hasReceiversFor(ScreenClickEvent.class)) {
				final ScreenClickEvent e = getEventInstance(ScreenClickEvent.class);
				e.setX(screenX);
				e.setY(screenY);
				e.setButton(button);
				sendEvent(e);
			}
			if (hasReceiversFor(MapClickEvent.class)) {
				final MapClickEvent e = getEventInstance(MapClickEvent.class);
				scratch.set(screenX, screenY);
				final Vector2 result = screenToMapConverter.apply(scratch);
				e.setX((int) result.x);
				e.setY((int) result.y);
				e.setButton(button);
				sendEvent(e);
			}
		} else {
			//
			// Handle drag-events.
			//
			if (hasReceiversFor(ScreenDragEndEvent.class)) {
				final ScreenDragEndEvent e = getEventInstance(ScreenDragEndEvent.class);
				e.setX(screenX);
				e.setY(screenY);
				sendEvent(e);
			}
			if (hasReceiversFor(MapDragEndEvent.class)) {
				final MapDragEndEvent e = getEventInstance(MapDragEndEvent.class);
				scratch.set(screenX, screenY);
				final Vector2 result = screenToMapConverter.apply(scratch);
				e.setX((int) result.x);
				e.setY((int) result.y);
				sendEvent(e);
			}
		}
		
		return true;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		
		if (!isOngoingDrag) {
			
			if (hasReceiversFor(ScreenDragStartEvent.class)) {
				final ScreenDragStartEvent e = getEventInstance(ScreenDragStartEvent.class);
				e.setX(screenX);
				e.setY(screenY);
				e.setButton(touchButton.get(pointer, Input.Buttons.LEFT));
				sendEvent(e);
			}
			if (hasReceiversFor(MapDragStartEvent.class)) {
				final MapDragStartEvent e = getEventInstance(MapDragStartEvent.class);
				scratch.set(screenX, screenY);
				final Vector2 result = screenToMapConverter.apply(scratch);
				e.setX((int) result.x);
				e.setY((int) result.y);
				e.setButton(touchButton.get(pointer, Input.Buttons.LEFT));
				sendEvent(e);
			}
			
			isOngoingDrag = true;
		} else {
			
			if (hasReceiversFor(ScreenDragUpdateEvent.class)) {
				final ScreenDragUpdateEvent e = getEventInstance(ScreenDragUpdateEvent.class);
				e.setX(screenX);
				e.setY(screenY);
				sendEvent(e);
			}
			if (hasReceiversFor(MapDragUpdateEvent.class)) {
				final MapDragUpdateEvent e = getEventInstance(MapDragUpdateEvent.class);
				scratch.set(screenX, screenY);
				final Vector2 result = screenToMapConverter.apply(scratch);
				e.setX((int) result.x);
				e.setY((int) result.y);
				sendEvent(e);
			}
		}
		
		return true;
	}
	
	@Override
	public boolean scrolled(float amountX, float amountY) {
		
		if (hasReceiversFor(ScrollEvent.class)) {
			final ScrollEvent e = getEventInstance(ScrollEvent.class);
			e.setAmountX(amountX);
			e.setAmountY(amountY);
			sendEvent(e);
		}
		
		return true;
	}
}
