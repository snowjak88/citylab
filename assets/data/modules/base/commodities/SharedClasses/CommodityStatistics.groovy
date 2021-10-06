import org.snowjak.city.util.Util

class CommodityStatistics {
	final static int N = 8
	final static float alpha = 2f / ( HISTORY_LENGTH + 1)
	final static int HISTORY_LENGTH = N * 2
	
	String commodityID
	
	transient float low, high, mean
	transient float recentTotalVolume, averageVolume
	transient float demand, supply
	
	final prices = new LinkedList()
	final volumes = new LinkedList()
	
	void reset() {
		recentTotalVolume = 0
		demand = 0
		supply = 0
	}
	
	void addPrice(float price) {
		prices << price
		while(prices.size() > HISTORY_LENGTH)
			prices.pop()
		
		low = Float.MAX_VALUE
		high = -Float.MAX_VALUE
		mean = 0
		def first = true
		prices.each { p ->
			low = Util.min( low, p )
			high = Util.max( high, p )
			mean = (first) ? p : p * alpha + (1f - alpha) * mean
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