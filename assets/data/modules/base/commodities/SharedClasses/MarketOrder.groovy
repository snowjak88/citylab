import com.badlogic.ashley.core.Entity

import org.codehaus.groovy.util.HashCodeHelper

import java.time.Instant

class MarketOrder implements Comparable<MarketOrder> {
	Entity owner
	String commodityID
	final Instant created = Instant.now()
	float quantity, price
	
	int compareTo(MarketOrder other) {
		final priceCompare = Float.compareTo( this.price, other.price )
		if(priceCompare != 0)
			return priceCompare
		created.compareTo other.created
	}
	
	int hashCode() {
		int result = HashCodeHelper.initHash()
		result = HashCodeHelper.updateHash(result, owner)
		result = HashCodeHelper.updateHash(result, commodityID)
		result
	}
}
