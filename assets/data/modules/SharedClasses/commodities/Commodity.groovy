package commodities

import network.IsNetworkNode

class Commodity {
	String id, title, description
	Set<Class<? extends IsNetworkNode>> validNetworks = []
}