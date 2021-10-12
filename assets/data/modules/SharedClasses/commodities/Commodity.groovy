package commodities

class Commodity {
	String id, title, description
	Set<Class<? extends IsNetworkNode>> validNetworks = []
}