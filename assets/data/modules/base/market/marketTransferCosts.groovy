//
// This system manages the background thread that computes
// commodity-trader connectivity.
//
// If a commodity-trader is also a network-node, then its transfer-costs to
// other such node-traders can be computed. 
//
tradingNodesListener = familyListener( Family.all( CanTradeCommodities, IsNetworkNode ).get() )