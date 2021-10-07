package trading

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable

import market.CommodityStatistics

class CanTradeCommodities implements Component, Poolable {
	
	/**
	 * This entity will buy commodities required for its consumption,
	 * and sell commodities not required, up to <em>N</em> time-units
	 * (as per its configured transmutations).
	 */
	float timeHorizon = 10
	
	final Map<String, CommodityStatistics> observedPrices = [:]
	final Map<String, PriceBelief> priceBeliefs = [:]
	
	void reset() {
		timeHorizon = 10
		observedPrices.clear()
		priceBeliefs.clear()
	}
	
	static class PriceBelief {
		String commodityID
		float min, max
	}
}