import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

class CanSellSurplusCommodities implements Component, Poolable {
	
	/**
	 * This entity will sell its surplus inventory down to <em>N</em>
	 * time-units of consumption (as per its configured transmutations).
	 */
	float timeHorizon = 10
	
	void reset() {
		timeHorizon = 10
	}
}