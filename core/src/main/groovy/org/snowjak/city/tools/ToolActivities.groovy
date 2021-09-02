package org.snowjak.city.tools

import org.snowjak.city.input.InputEventReceiver
import org.snowjak.city.input.MapClickEvent
import org.snowjak.city.input.MapHoverEvent
import org.snowjak.city.service.GameService
import org.snowjak.city.tools.activity.Activity
import org.snowjak.city.tools.activity.InputReceivingActivity

import com.badlogic.gdx.Input

class ToolActivities {
	
	private final Tool tool
	private final GameService gameService
	
	public ToolActivities(Tool tool, GameService gameService) {
		this.tool = tool
		this.gameService = gameService
	}
	
	/**
	 * Register a map-hover activity. While the associated tool is active, this activity will be
	 * called every frame that <em>none</em> of the cursor's buttons are depressed. This activity will be
	 * passed the coordinates of the map-cell that the cursor is over. (<strong>Note</strong> that the given map-cell may
	 * not be a valid cell at all!)
	 * @param mapHoverSpec
	 * @return
	 */
	public Activity mapHover(@DelegatesTo(value=MapCoordReceiver, strategy=Closure.DELEGATE_FIRST) Closure mapHoverSpec) {
		
		mapHoverSpec = mapHoverSpec.rehydrate(tool, this, mapHoverSpec)
		mapHoverSpec.resolveStrategy = Closure.DELEGATE_FIRST
		final mapHoverReceiver = mapHoverSpec as MapCoordReceiver
		final adapter = [ receive: { MapHoverEvent e ->
				mapHoverReceiver.receive e.x, e.y
			} ] as InputEventReceiver<MapHoverEvent>
		
		final activity = new InputReceivingActivity(tool, gameService, MapHoverEvent, adapter)
		tool.activities << activity
		activity
	}
	
	/**
	 * Register a map-click activity. While the associated tool is active, this activity will
	 * be called whenever the user clicks a map-cell with any mouse-button. This activity will
	 * be passed the coordinates of the map-cell that was clicked, along with the button-# (c.f. {@link Input.Buttons}).
	 * (<strong>Note</strong> that the given map-cell may not be a valid cell at all!)
	 * @param mapClickSpec
	 * @return
	 */
	public Activity mapClick(@DelegatesTo(value=MapCoordButtonfulReceiver, strategy=Closure.DELEGATE_FIRST) Closure mapClickSpec) {
		
		mapClickSpec = mapClickSpec.rehydrate(tool, this, mapClickSpec)
		mapClickSpec.resolveStrategy = Closure.DELEGATE_FIRST
		final mapClickReceiver = mapClickSpec as MapCoordButtonfulReceiver
		
		final adapter = [ receive: { MapClickEvent e ->
				mapClickReceiver.receive e.x, e.y, e.button
			} ] as InputEventReceiver<MapClickEvent>
		
		final activity = new InputReceivingActivity(tool, gameService, MapClickEvent, adapter)
		tool.activities << activity
		activity
	}
	
	/**
	 * Register a map-click activity. While the associated tool is active, this activity will
	 * be called whenever the user clicks a map-cell with the specified mouse-button. This activity will
	 * be passed the coordinates of the map-cell that was clicked.
	 * (<strong>Note</strong> that the given map-cell may not be a valid cell at all!)
	 * @param button c.f. {@link Input.Buttons}
	 * @param mapClickSpec
	 * @return
	 */
	public Activity mapClick(int button, @DelegatesTo(value=MapCoordButtonfulReceiver, strategy=Closure.DELEGATE_FIRST) Closure mapClickSpec) {
		
		mapClickSpec = mapClickSpec.rehydrate(tool, this, mapClickSpec)
		mapClickSpec.resolveStrategy = Closure.DELEGATE_FIRST
		final mapClickReceiver = mapClickSpec as MapCoordReceiver
		
		final requiredButton = button
		final adapter = [ receive: { MapClickEvent e ->
				if(e.button == requiredButton)
					mapClickReceiver.receive e.x, e.y
			} ] as InputEventReceiver<MapClickEvent>
		
		final activity = new InputReceivingActivity(tool, gameService, MapClickEvent, adapter)
		tool.activities << activity
		activity
	}
	
	@FunctionalInterface
	public interface MapCoordReceiver {
		public void receive(float cellX, float cellY)
	}
	
	@FunctionalInterface
	public interface MapCoordButtonfulReceiver {
		public void receive(float cellX, float cellY, int button)
	}
}
