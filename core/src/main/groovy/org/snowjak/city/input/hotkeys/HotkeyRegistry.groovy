/**
 * 
 */
package org.snowjak.city.input.hotkeys

import org.snowjak.city.input.InputEventReceiver
import org.snowjak.city.input.KeyTypedEvent

import com.badlogic.gdx.Input

/**
 * Registry and dispatcher for {@link Hotkey}s. Must be registered as an
 * {@link InputEventReceiver} with the active {@link GameInputProcessor}.
 * <p>
 * Maps Hotkeys to actions (i.e., one or more {@link Runnable}s). The registry
 * is checked whenever a Hotkey is {@link #register(Hotkey,Runnable[]) registered},
 * {@link #unregister(Hotkey) unregistered}, or {@link #update(Hotkey,Hotkey) remapped}.
 * All Hotkeys which {@link Hotkey#matches(Hotkey) match} any other registered Hotkey
 * get marked as {@link Hotkey#isColliding "colliding"} -- making them <strong>ineligible</strong>
 * to be activated by {@link KeyTypedEvent}s.
 * </p>
 * 
 * @author snowjak88
 *
 */
class HotkeyRegistry implements InputEventReceiver<KeyTypedEvent> {
	
	private final Map<Hotkey,Collection<Runnable>> hotkeyActions = new LinkedHashMap<>()
	private final Set<Hotkey>[] hotkeysByKeycode = new Set[Input.Keys.MAX_KEYCODE]
	
	/**
	 * Register the given Hotkey. If this registration results in a collision -- i.e.,
	 * any situation where two or more Hotkeys {@link Hotkey#matches(Hotkey) match each other},
	 * marks all such Hotkeys as "colliding".
	 *
	 * @param hotkey
	 * @param actions
	 */
	public void register(Hotkey hotkey, Runnable... actions) {
		register hotkey, Arrays.asList(actions)
	}
	
	/**
	 * Register the given Hotkey. If this registration results in a collision -- i.e.,
	 * any situation where two or more Hotkeys {@link Hotkey#matches(Hotkey) match each other},
	 * marks all such Hotkeys as "colliding".
	 * 
	 * @param hotkey
	 * @param actions
	 */
	public void register(Hotkey hotkey, Collection<Runnable> actions) {
		
		if(hotkeysByKeycode[hotkey.keycode] == null)
			hotkeysByKeycode[hotkey.keycode] = new HashSet<>()
		
		hotkeysByKeycode[hotkey.keycode].add hotkey
		hotkeyActions[hotkey] = new LinkedList<>(actions)
		
		updateColliding hotkey.keycode
	}
	
	public void unregister(Hotkey hotkey) {
		
		hotkeyActions.remove hotkey
		hotkeysByKeycode[hotkey.keycode].remove hotkey
		
		updateColliding hotkey.keycode
	}
	
	public void change(Hotkey from, Hotkey to) {
		if(!hotkeyActions.containsKey(from))
			return
		
		final Collection<Runnable> actions = hotkeyActions[from]
		unregister from
		
		if(actions == null)
			return
		
		register to, actions
	}
	
	private void updateColliding(int onlyForKeycode = -1) {
		
		final checkThese = (onlyForKeycode > -1) ? hotkeysByKeycode[onlyForKeycode] : hotkeyActions.keySet()
		
		for(def checkThis : checkThese) {
			
			final collidingWith = hotkeysByKeycode[checkThis.keycode].findAll { it !== checkThis && it.matches(checkThis) }
			
			checkThis.isColliding = !(collidingWith.isEmpty())
			collidingWith.each { it.isColliding = true }
		}
	}
	
	@Override
	public void receive(KeyTypedEvent event) {
		
		hotkeysByKeycode[event.keycode]?.forEach {
			if(it.matches(event))
				hotkeyActions[it].each { it.run() }
		}
	}
}
