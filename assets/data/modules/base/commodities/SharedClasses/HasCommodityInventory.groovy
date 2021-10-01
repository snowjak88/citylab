import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

class HasCommodityInventory implements Component, Poolable {
	
	/**
	 * Commodity-IDs and quantity on-hand
	 */
	final Map<String, Float> inventory = [:]
	
	void reset() {
		inventory.clear()
	}
}