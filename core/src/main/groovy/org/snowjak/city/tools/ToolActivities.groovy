package org.snowjak.city.tools

import org.snowjak.city.input.InputEventReceiver
import org.snowjak.city.input.MapHoverEvent
import org.snowjak.city.service.GameService
import org.snowjak.city.tools.activity.Activity
import org.snowjak.city.tools.activity.InputReceivingActivity

class ToolActivities {
	
	private final Tool tool
	private final GameService gameService
	
	public ToolActivities(Tool tool, GameService gameService) {
		this.tool = tool
		this.gameService = gameService
	}
	
	public Activity mapHover(@DelegatesTo(value=MapHoverReceiver, strategy=Closure.DELEGATE_FIRST) Closure mapHoverSpec) {
		
		mapHoverSpec = mapHoverSpec.rehydrate(tool, this, mapHoverSpec)
		//		mapHoverSpec.delegate = tool
		mapHoverSpec.resolveStrategy = Closure.DELEGATE_FIRST
		final mapHoverReceiver = mapHoverSpec as MapHoverReceiver
		final adapter = [ receive: { MapHoverEvent e -> 
			mapHoverReceiver.receive e.x, e.y 
			} ] as InputEventReceiver<MapHoverEvent>
		
		final activity = new InputReceivingActivity(tool, gameService, MapHoverEvent, adapter)
		tool.activities << activity
		activity
	}
	
	@FunctionalInterface
	public interface MapHoverReceiver {
		public void receive(float cellX, float cellY);
	}
}
