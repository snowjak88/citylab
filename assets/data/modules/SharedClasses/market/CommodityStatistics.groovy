package market

import org.snowjak.city.util.Util

class CommodityStatistics {
	final static int N = 8
	final static float alpha = 2f / ( HISTORY_LENGTH + 1)
	final static int HISTORY_LENGTH = N * 2
	
	String commodityID
	
	transient float sellerLow, sellerHigh, sellerMean
	transient float buyerLow, buyerHigh, buyerMean
	transient float recentTotalVolume, averageVolume
	transient float demand, supply
	
	final sellerPrices = new LinkedList()
	final buyerPrices = new LinkedList()
	final volumes = new LinkedList()
	
	void reset() {
		recentTotalVolume = 0
		demand = 0
		supply = 0
	}
	
	void addPrice(float sellerPrice, buyerPrice) {
		sellerPrices << sellerPrice
		while(sellerPrices.size() > HISTORY_LENGTH)
			sellerPrices.pop()
		
		sellerLow = Float.MAX_VALUE
		sellerHigh = -Float.MAX_VALUE
		sellerMean = 0
		def first = true
		sellerPrices.each { p ->
			sellerLow = Util.min( sellerLow, p )
			sellerHigh = Util.max( sellerHigh, p )
			sellerMean = (first) ? p : p * alpha + (1f - alpha) * sellerMean
			first = false
		}
		
		//
		
		buyerPrices << buyerPrice
		while(buyerPrices.size() > HISTORY_LENGTH)
			buyerPrices.pop()
		
		buyerLow = Float.MAX_VALUE
		buyerHigh = -Float.MAX_VALUE
		buyerMean = 0
		first = true
		buyerPrices.each { p ->
			buyerLow = Util.min( buyerLow, p )
			buyerHigh = Util.max( buyerHigh, p )
			buyerMean = (first) ? p : p * alpha + (1f - alpha) * buyerMean
			first = false
		}
	}
	
	void addVolume(float volume) {
		volumes << volume
		while(volumes.size() > HISTORY_LENGTH)
			volumes.pop()
		averageVolume = 0
		def first = true
		volumes.each { v ->
			averageVolume = (first) ? v : v * alpha + (1f - alpha) * averageVolume
			first = false
		}
		recentTotalVolume += volume
	}
	
	void addDemand(float volume) {
		demand += volume
	}
	
	void addSupply(float volume) {
		supply += volume
	}
}