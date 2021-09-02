package org.snowjak.city.tools.activity

import org.snowjak.city.input.AbstractInputEvent
import org.snowjak.city.input.InputEventReceiver
import org.snowjak.city.service.GameService
import org.snowjak.city.tools.Tool
import org.snowjak.city.util.UnregistrationHandle

/**
 * Tool activity that delegates to an {@link InputEventReceiver} of some kind.
 * 
 * @author snowjak88
 *
 */
class InputReceivingActivity<E extends AbstractInputEvent> extends Activity implements InputEventReceiver<E> {
	
	final GameService gameService
	final Class<E> inputEventType
	final InputEventReceiver<E> inputReceiver
	
	private UnregistrationHandle inputReceiverUnregisterer
	
	public InputReceivingActivity(Tool tool, GameService gameService, Class<E> inputEventType, InputEventReceiver<E> inputReceiver) {
		super(tool);
		
		this.gameService = gameService
		this.inputEventType = inputEventType
		this.inputReceiver = inputReceiver
	}
	
	@Override
	public void activate() {
		
		inputReceiverUnregisterer = gameService.state.inputProcessor?.register inputEventType, this
	}
	
	@Override
	public void update() {
		//
		// Nothing to do here -- the receiver is updated by receive() instead.
	}
	
	@Override
	public void deactivate() {
		
		inputReceiverUnregisterer?.unregisterMe()
		inputReceiverUnregisterer = null
	}
	
	@Override
	public void receive(E event) {
		
		inputReceiver.receive event
	}
}
