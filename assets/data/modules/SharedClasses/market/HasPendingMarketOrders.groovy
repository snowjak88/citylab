package market

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool.Poolable

import org.codehaus.groovy.util.HashCodeHelper

import market.MarketOrder

class HasPendingMarketOrders implements Component, Poolable {
	
	/**
	 * Ask orders by commodity-ID
	 */
	final Map<String,MarketOrder> asks = [:]
	/**
	 * Bid-orders by commodity-ID
	 */
	final Map<String,MarketOrder> bids = [:]
	
	void reset() {
		asks.clear()
		bids.clear()
	}
}