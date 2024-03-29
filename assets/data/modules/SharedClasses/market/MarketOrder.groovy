package market

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool.Poolable

import org.codehaus.groovy.util.HashCodeHelper

class MarketOrder implements Comparable<MarketOrder>, Poolable {
	Entity owner
	String commodityID
	float originalQuantity, quantity, price
	boolean complete = false, analyzedPostSale = false, delivered = false
	final List<Fulfillment> fulfilled = []
	
	void addFulfilled(float price, float quantity) {
		fulfilled << [ price: price, quantity: quantity ] as Fulfillment
	}
	
	int compareTo(MarketOrder other) {
		Float.compare( this.price, other.price )
	}
	
	int hashCode() {
		int result = HashCodeHelper.initHash()
		result = HashCodeHelper.updateHash(result, owner)
		result = HashCodeHelper.updateHash(result, commodityID)
		result
	}
	
	void reset() {
		owner = null
		commodityID = null
		originalQuantity = 0
		quantity = 0
		price = 0
		complete = false
		analyzedPostSale = false
		delivered = false
		fulfilled.clear()
	}
	
	public static class Fulfillment {
		float quantity = 0
		float price = 0
	}
}